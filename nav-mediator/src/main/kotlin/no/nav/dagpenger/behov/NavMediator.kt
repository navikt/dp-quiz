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

        seksjon.filter { it.godkjentType() && it.erUbesvart() && it.alleAvhengigFaktumBesvart() }.forEach {
            val visitor = Visitor(it)
            when (it.id) {
                "1" -> behovJson(fnr, søknadUuid, "ØnskerDagpengerFraDato")
                "2" -> behovJson(fnr, søknadUuid, "SisteDagMedArbeidsplikt")
                "3" -> behovJson(fnr, søknadUuid, "Registreringsdato")
                "4" -> behovJson(fnr, søknadUuid, "SisteDagMedLønn")
                "6" -> behovJson(fnr, søknadUuid, "EgenNæring")
                "7" -> behovJson(
                    fnr,
                    søknadUuid,
                    "InntektSiste3År",
                    "EgenNæring" to visitor.avhengerAv["6"]!!.svar(),
                    "Virkningstidspunkt" to visitor.avhengerAv["5"]!!.svar()
                )
                "11" -> behovJson(fnr, søknadUuid, "Søknadstidspunkt")
                "12" -> behovJson(fnr, søknadUuid, "Verneplikt")
                "14" -> behovJson(fnr, søknadUuid, "GodkjenningDokumentasjonFangstOgFisk")
                else -> throw IllegalArgumentException("Ukjent faktum id ${it.id}")
            }.also {
                rapidsConnection.publish(it)
            }
        }
    }

    private fun behovJson(fnr: String, søknadUuid: UUID, behovType: String, vararg svar: Pair<String, Any>) = JsonMessage.newMessage(
        mutableMapOf(
            "@behov" to behovType,
            "@id" to UUID.randomUUID(),
            "fnr" to fnr,
            "søknadUuid" to søknadUuid
        ) + svar.asList().toMap()
    ).toJson()
}

private fun Faktum<*>.godkjentType(): Boolean {
    return this !is UtledetFaktum<*>
}

private fun Faktum<*>.erUbesvart() = !this.erBesvart()

private class Visitor(faktum: Faktum<*>) : FaktumVisitor {
    var alleAvhengigFaktumBesvart = true
    val avhengerAv = mutableMapOf<String, Faktum<*>>()

    init {
        faktum.accept(this)
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

private fun <R : Comparable<R>> Faktum<R>.alleAvhengigFaktumBesvart(): Boolean {
    val visitor = Visitor(this)
    return visitor.alleAvhengigFaktumBesvart
}
