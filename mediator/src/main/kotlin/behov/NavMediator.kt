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

class NavMediator(private val rapidsConnection: RapidsConnection) {

    fun sendBehov(versjon: Int, seksjon: Seksjon, fnr: String, søknadUuid: UUID) {
        seksjon.map { BehovBuilder(it, FaktumBehov.id(versjon)) } // TODO: Fakta må ha NAV-roller
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
