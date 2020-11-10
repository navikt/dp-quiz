package no.nav.dagpenger.behov

import no.nav.dagpenger.model.faktagrupper.Seksjon
import no.nav.dagpenger.model.faktum.Faktum
import no.nav.dagpenger.model.faktum.GrunnleggendeFaktum
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.visitor.FaktumVisitor
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.RapidsConnection
import java.util.UUID

class NavMediator(private val rapidsConnection: RapidsConnection) {
    fun sendBehov(seksjon: Seksjon, fnr: String, søknadUuid: UUID) {

        seksjon.filter { it.erUbesvart() && it.alleAvhengigFaktumBesvart() }.forEach {
            when (it.id) {
                "12" -> behovJson(fnr, søknadUuid, "Verneplikt")
                "6" -> behovJson(fnr, søknadUuid, "EgenNæring")
                else -> throw IllegalArgumentException("Ukjent faktum id ${it.id}")
            }.also {
                rapidsConnection.publish(it)
            }
        }
    }

    private fun behovJson(fnr: String, søknadUuid: UUID, behovType: String) = JsonMessage.newMessage(
        mutableMapOf(
            "@behov" to behovType,
            "@id" to UUID.randomUUID(),
            "fnr" to fnr,
            "søknadUuid" to søknadUuid
        )
    ).toJson()
}

private fun Faktum<*>.erUbesvart() = !this.erBesvart()

private class Visitor(faktum: Faktum<*>) : FaktumVisitor {
    var alleAvhengigFaktumBesvart = true

    init {
        faktum.accept(this)
    }

    override fun <R : Comparable<R>> visit(faktum: GrunnleggendeFaktum<R>, tilstand: Faktum.FaktumTilstand, id: String, avhengigeFakta: Set<Faktum<*>>, avhengerAvFakta: Set<Faktum<*>>, roller: Set<Rolle>, clazz: Class<R>) {
        alleAvhengigFaktumBesvart = avhengerAvFakta.all { it.erBesvart() }
    }
}

private fun <R : Comparable<R>> Faktum<R>.alleAvhengigFaktumBesvart(): Boolean {
    val visitor = Visitor(this)
    return visitor.alleAvhengigFaktumBesvart
}
