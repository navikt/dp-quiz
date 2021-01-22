package no.nav.dagpenger.quiz.mediator.meldinger

import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River
import no.nav.helse.rapids_rivers.asLocalDate

internal class SenesteMuligeVirkningstidspunktService(rapidsConnection: RapidsConnection) :
    River.PacketListener {
    init {
        River(rapidsConnection).apply {
            validate { it.demandAll("@behov", listOf("SenesteMuligeVirkningstidspunkt")) }
            validate { it.forbid("@løsning") }
            validate { it.requireKey("Behandlingsdato") }
        }.register(this)
    }

    override fun onPacket(packet: JsonMessage, context: RapidsConnection.MessageContext) {
        val dagensDato = packet["Behandlingsdato"].asLocalDate()
        packet["@løsning"] = mapOf(
            "SenesteMuligeVirkningstidspunkt" to dagensDato.plusDays(14)
        )

        context.send(packet.toJson())
    }
}
