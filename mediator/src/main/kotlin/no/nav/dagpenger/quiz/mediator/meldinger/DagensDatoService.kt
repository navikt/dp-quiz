package no.nav.dagpenger.quiz.mediator.meldinger

import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River
import java.time.LocalDate

internal class DagensDatoService(rapidsConnection: RapidsConnection) :
    River.PacketListener {
    init {
        River(rapidsConnection).apply {
            validate { it.demandAll("@behov", listOf("DagensDato", "GrenseDato")) }
            validate { it.forbid("@løsning") }
        }.register(this)
    }

    override fun onPacket(packet: JsonMessage, context: RapidsConnection.MessageContext) {
        packet["@løsning"] = mapOf(
            "DagensDato" to LocalDate.now(),
            "GrenseDato" to LocalDate.now().plusDays(14)
        )

        context.send(packet.toJson())
    }
}
