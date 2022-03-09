package no.nav.dagpenger.quiz.mediator.behovløsere

import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River
import java.time.LocalDate

internal class AvviklingsdatoKravTilMinsteinntektService(rapidsConnection: RapidsConnection) :
    River.PacketListener {
    init {
        River(rapidsConnection).apply {
            validate { it.demandAll("@behov", listOf("AvviklingsdatoKravTilMinsteinntekt")) }
            validate { it.forbid("@løsning") }
        }.register(this)
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) {
        packet["@løsning"] = mapOf(
            "AvviklingsdatoKravTilMinsteinntekt" to LocalDate.of(2022, 4, 1)
        )

        context.publish(packet.toJson())
    }
}
