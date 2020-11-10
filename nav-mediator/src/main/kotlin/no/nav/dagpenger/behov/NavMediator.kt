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

        seksjon.map{it to Visitor(it)}
                .filter { (faktum, visitor) -> faktum.godkjentType() && faktum.erUbesvart() && visitor.alleAvhengigFaktumBesvart }
                .forEach { (faktum, visitor) ->

            if (faktum.id == "7") {
                behovJson(
                    fnr,
                    søknadUuid,
                        faktum.id,
                    "EgenNæring" to visitor.avhengerAv["6"]!!.svar(),
                    "Virkningstidspunkt" to visitor.avhengerAv["5"]!!.svar()
                )
            } else {
                behovJson(fnr, søknadUuid, faktum.id)
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
