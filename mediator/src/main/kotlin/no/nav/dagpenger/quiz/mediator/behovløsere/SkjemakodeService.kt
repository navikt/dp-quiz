package no.nav.dagpenger.quiz.mediator.behovløsere

import mu.KotlinLogging
import mu.withLoggingContext
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River

internal class SkjemakodeService(rapidsConnection: RapidsConnection) : River.PacketListener {

    private companion object {
        val logger = KotlinLogging.logger { }
    }

    init {
        River(rapidsConnection).apply {
            validate { it.demandValue("@event_name", "behov") }
            validate { it.demandAll("@behov", listOf("Skjemakode")) }
            validate { it.requireKey("søknad_uuid") }
            validate { it.forbid("@løsning") }
        }.register(this)
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) {
        withLoggingContext("søknadId" to packet["søknad_uuid"].asText()) {
            packet["@løsning"] = mapOf(
                "Skjemakode" to mapOf("tittel" to "Søknad om dagpenger (ikke permittert)", "skjemakode" to "04-01.03")
            )

            context.publish(packet.toJson())
            logger.info { "Løser behov for skjemakode" }
        }
    }
}
