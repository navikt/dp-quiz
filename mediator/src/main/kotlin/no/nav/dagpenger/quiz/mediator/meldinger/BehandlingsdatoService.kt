package no.nav.dagpenger.quiz.mediator.meldinger

import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River
import java.time.LocalDate

internal class BehandlingsdatoService(rapidsConnection: RapidsConnection) :
    River.PacketListener {
    init {
        River(rapidsConnection).apply {
            validate { it.demandAll("@behov", listOf("Behandlingsdato")) }
            validate { it.forbid("@løsning") }
        }.register(this)
    }

    override fun onPacket(packet: JsonMessage, context: RapidsConnection.MessageContext) {
        packet["@løsning"] = mapOf(
            "Behandlingsdato" to LocalDate.now()
        )

        context.send(packet.toJson())
    }
}