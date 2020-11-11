package no.nav.dagpenger.behov

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
    fun sendBehov(seksjon: Seksjon, fnr: String, søknadUuid: UUID) {

        seksjon.map { it to BehovBuilder(it) }.filter { (_, behovBuilder) -> behovBuilder.behovKanSendes }.forEach { (faktum, behovBuilder) ->
            behovBuilder.build(fnr, søknadUuid).also {
                rapidsConnection.publish(it)
            }
        }
    }
}

private fun Faktum<*>.godkjentType(): Boolean {
    return this !is UtledetFaktum<*>
}

private fun Faktum<*>.erUbesvart() = !this.erBesvart()

private class BehovBuilder(private val faktum: Faktum<*>) : FaktumVisitor {
    var alleAvhengigFaktumBesvart = true
    val avhengerAv = mutableMapOf<String, Faktum<*>>()

    init {
        faktum.accept(this)
    }

    val behovKanSendes by lazy {
        faktum.godkjentType() && faktum.erUbesvart() && alleAvhengigFaktumBesvart
    }

    fun build(fnr: String, søknadUuid: UUID): String {
        return JsonMessage.newMessage(
            mutableMapOf(
                "@behov" to BehovType.fromId(faktum.id).name,
                "@id" to UUID.randomUUID(),
                "faktumId" to faktum.id,
                "fnr" to fnr,
                "søknadUuid" to søknadUuid
            ) + avhengerAv.map { (id, faktum) -> BehovType.fromId(id).name to faktum.svar() }
        ).toJson()
    }

    override fun <R : Comparable<R>> visit(faktum: GrunnleggendeFaktum<R>, tilstand: Faktum.FaktumTilstand, id: String, avhengigeFakta: Set<Faktum<*>>, avhengerAvFakta: Set<Faktum<*>>, roller: Set<Rolle>, clazz: Class<R>) {
        alleAvhengigFaktumBesvart = avhengerAvFakta.all { it.erBesvart() }
        avhengerAvFakta.forEach { avhengerAv[it.id] = it }
    }

    override fun <R : Comparable<R>> preVisit(faktum: UtledetFaktum<R>, id: String, avhengigeFakta: Set<Faktum<*>>, avhengerAvFakta: Set<Faktum<*>>, children: Set<Faktum<*>>, clazz: Class<R>, regel: FaktaRegel<R>) {
        alleAvhengigFaktumBesvart = avhengerAvFakta.all { it.erBesvart() }
        avhengerAvFakta.forEach { avhengerAv[it.id] = it }
    }
}

private enum class BehovType(private val id: String) {
    ØnskerDagpengerFraDato("1"),
    SisteDagMedArbeidsplikt("2"),
    Registreringsdato("3"),
    SisteDagMedLønn("4"),
    Virkningstidspunkt("5"),
    EgenNæring("6"),
    InntektSiste3År("7"),
    InntektSiste12Mnd("8"),
    G3("9"),
    G15("10"),
    Søknadstidspunkt("11"),
    Verneplikt("12"),
    GodkjenningDokumentasjonFangstOgFisk("14");

    companion object {
        fun fromId(id: String) = values().firstOrNull() { it.id == id } ?: throw IllegalArgumentException("Ukjent faktum id $id")
    }
}
