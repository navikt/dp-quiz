package no.nav.dagpenger.behov

import no.nav.dagpenger.model.faktagrupper.Seksjon
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.RapidsConnection
import java.util.UUID

class NavMediator(private val rapidsConnection: RapidsConnection) {
    fun sendBehov(seksjon: Seksjon, fnr: String, søknadUuid: UUID) {

        seksjon.filterNot { it.erBesvart() }.forEach {
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
