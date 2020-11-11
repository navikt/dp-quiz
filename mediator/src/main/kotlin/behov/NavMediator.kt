package behov

import no.nav.dagpenger.model.factory.FaktaRegel
import no.nav.dagpenger.model.faktagrupper.Seksjon
import no.nav.dagpenger.model.faktum.Faktum
import no.nav.dagpenger.model.faktum.GrunnleggendeFaktum
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.faktum.UtledetFaktum
import no.nav.dagpenger.model.visitor.FaktumVisitor
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.RapidsConnection
import java.util.UUID

class FaktumBehov(private val delegate: Map<Int, String>) : Map<Int, String> by delegate {
    override operator fun get(id: Int): String = delegate[id] ?: throw IllegalArgumentException("Ukjent faktum id $id")
}

val BehovtypeVersjon1 = mapOf<Int, String>(
    1 to "ØnskerDagpengerFraDato",
    2 to "SisteDagMedArbeidsplikt",
    3 to "Registreringsdato",
    4 to "SisteDagMedLønn",
    5 to "Virkningstidspunkt",
    6 to "EgenNæring",
    7 to "InntektSiste3År",
    8 to "InntektSiste12Mnd",
    9 to "G3",
    10 to "G15",
    11 to "Søknadstidspunkt",
    12 to "Verneplikt",
    14 to "GodkjenningDokumentasjonFangstOgFisk"
)

class NavMediator(private val rapidsConnection: RapidsConnection) {

    val versjonToBuilder = mapOf(1 to FaktumBehov(BehovtypeVersjon1))

    fun sendBehov(versjon: Int, seksjon: Seksjon, fnr: String, søknadUuid: UUID) {
        require(versjonToBuilder.containsKey(versjon)) { "Vet ikke om versjon $versjon" }
        seksjon.map { BehovBuilder(it, versjonToBuilder[versjon]!!) } // TODO: Fakta må ha NAV-roller
            .filter { behovBuilder -> behovBuilder.behovKanSendes }.forEach { behovBuilder ->
                behovBuilder.build(fnr, søknadUuid).also {
                    rapidsConnection.publish(it)
                }
            }
    }
}

private class BehovBuilder(private val faktum: Faktum<*>, private val faktumBehov: FaktumBehov) : FaktumVisitor {
    private var alleAvhengigFaktumBesvart = true
    private val avhengerAv = mutableMapOf<String, Faktum<*>>()

    init {
        faktum.accept(this)
    }

    val behovKanSendes by lazy {
        faktum.godkjentType() && faktum.erUbesvart() && alleAvhengigFaktumBesvart
    }

    fun build(fnr: String, søknadUuid: UUID): String {
        return JsonMessage.newMessage(
            mutableMapOf(
                "@behov" to faktumBehov[faktum.rootId],
                "@id" to UUID.randomUUID(),
                "faktumId" to faktum.id,
                "fnr" to fnr,
                "søknadUuid" to søknadUuid
            ) + avhengerAv.map { (id, faktum) -> faktumBehov[faktum.rootId] to faktum.svar() }
        ).toJson()
    }

    private fun Faktum<*>.godkjentType(): Boolean {
        return this !is UtledetFaktum<*>
    }

    private fun Faktum<*>.erUbesvart() = !this.erBesvart()

    private val Faktum<*>.rootId get() = this.reflection { rootId, _ -> rootId }

    override fun <R : Comparable<R>> visit(
        faktum: GrunnleggendeFaktum<R>,
        tilstand: Faktum.FaktumTilstand,
        id: String,
        avhengigeFakta: Set<Faktum<*>>,
        avhengerAvFakta: Set<Faktum<*>>,
        roller: Set<Rolle>,
        clazz: Class<R>
    ) {
        alleAvhengigFaktumBesvart = avhengerAvFakta.all { it.erBesvart() }
        avhengerAvFakta.forEach { avhengerAv[it.id] = it }
    }

    override fun <R : Comparable<R>> preVisit(
        faktum: UtledetFaktum<R>,
        id: String,
        avhengigeFakta: Set<Faktum<*>>,
        avhengerAvFakta: Set<Faktum<*>>,
        children: Set<Faktum<*>>,
        clazz: Class<R>,
        regel: FaktaRegel<R>
    ) {
        alleAvhengigFaktumBesvart = avhengerAvFakta.all { it.erBesvart() }
        avhengerAvFakta.forEach { avhengerAv[it.id] = it }
    }
}
