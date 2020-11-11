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

        seksjon.map { it to BehovBuilder(it) }.filter { (faktum, behovBuilder) -> behovBuilder.behovKanSendes }.forEach { (faktum, behovBuilder) ->

            when (faktum.id) {
                "1" -> behovJson(faktum.id, fnr, søknadUuid, "ØnskerDagpengerFraDato")
                "2" -> behovJson(faktum.id, fnr, søknadUuid, "SisteDagMedArbeidsplikt")
                "3" -> behovJson(faktum.id, fnr, søknadUuid, "Registreringsdato")
                "4" -> behovJson(faktum.id, fnr, søknadUuid, "SisteDagMedLønn")
                "6" -> behovJson(faktum.id, fnr, søknadUuid, "EgenNæring")
                "7" -> behovJson(
                    faktum.id,
                    fnr,
                    søknadUuid,
                    "InntektSiste3År",
                    "EgenNæring" to behovBuilder.avhengerAv["6"]!!.svar(),
                    "Virkningstidspunkt" to behovBuilder.avhengerAv["5"]!!.svar()
                )
                "8" -> behovJson(faktum.id, fnr, søknadUuid, "InntektSiste12Mnd")
                "9" -> behovJson(faktum.id, fnr, søknadUuid, "3G")
                "10" -> behovJson(faktum.id, fnr, søknadUuid, "1.5G")
                "11" -> behovJson(faktum.id, fnr, søknadUuid, "Søknadstidspunkt")
                "12" -> behovJson(faktum.id, fnr, søknadUuid, "Verneplikt")
                "14" -> behovJson(faktum.id, fnr, søknadUuid, "GodkjenningDokumentasjonFangstOgFisk")
                else -> throw IllegalArgumentException("Ukjent faktum id ${faktum.id}")
            }.also {
                rapidsConnection.publish(it)
            }
        }
    }

    private fun behovJson(faktumId: String, fnr: String, søknadUuid: UUID, behovType: String, vararg svar: Pair<String, Any>) = JsonMessage.newMessage(
        mutableMapOf(
            "@behov" to behovType,
            "@id" to UUID.randomUUID(),
            "faktumId" to faktumId,
            "fnr" to fnr,
            "søknadUuid" to søknadUuid
        ) + svar.asList().toMap()
    ).toJson()
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
    val visitor = BehovBuilder(this)
    return visitor.alleAvhengigFaktumBesvart
}
