package no.nav.dagpenger.quiz.mediator.meldinger

import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River

internal class TerskelFaktorService(rapidsConnection: RapidsConnection) :
    River.PacketListener {
    init {
        River(rapidsConnection).apply {
            validate { it.demandAll("@behov", listOf("ØvreTerskelFaktor", "NedreTerskelFaktor")) }
            validate { it.forbid("@løsning") }
        }.register(this)
    }

    override fun onPacket(packet: JsonMessage, context: RapidsConnection.MessageContext) {
        packet["@løsning"] = mapOf(
            "ØvreTerskelFaktor" to 3.0,
            "NedreTerskelFaktor" to 1.5
        )

        context.send(packet.toJson())
    }
}
