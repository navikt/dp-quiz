package no.nav.dagpenger.quiz.mediator.meldinger

import mu.KotlinLogging
import no.nav.dagpenger.quiz.mediator.db.SøknadPersistence
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River
import no.nav.helse.rapids_rivers.withMDC
import java.util.UUID

internal class SøknadSlettetService(
    rapidsConnection: RapidsConnection,
    private val søknadPersistence: SøknadPersistence
) : River.PacketListener {

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    init {
        River(rapidsConnection).apply {
            validate {
                it.demandValue("@event_name", "søknad_slettet")
                it.requireKey("søknad_uuid")
            }
        }.register(this)
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) {
        val uuid = UUID.fromString(packet["søknad_uuid"].asText())
        withMDC("søknad_uuid" to uuid.toString()) {
            try {
                søknadPersistence.slett(uuid)
                logger.info { "Søknad slettet" }
            } catch (e: Exception) {
                logger.error { "Sletting av søknad med uuid: $uuid feilet" }
            }
        }
    }
}
