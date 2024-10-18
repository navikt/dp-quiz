package no.nav.dagpenger.quiz.mediator.behovløsere

import com.github.navikt.tbd_libs.rapids_and_rivers.JsonMessage
import com.github.navikt.tbd_libs.rapids_and_rivers.River
import com.github.navikt.tbd_libs.rapids_and_rivers.asLocalDate
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageContext
import com.github.navikt.tbd_libs.rapids_and_rivers_api.RapidsConnection

internal class SenesteMuligeVirkningsdatoService(rapidsConnection: RapidsConnection) :
    River.PacketListener {
    init {
        River(rapidsConnection).apply {
            validate { it.demandAll("@behov", listOf("SenesteMuligeVirkningstidspunkt")) }
            validate { it.forbid("@løsning") }
            validate { it.requireKey("Behandlingsdato") }
        }.register(this)
    }

    override fun onPacket(
        packet: JsonMessage,
        context: MessageContext,
    ) {
        val dagensDato = packet["Behandlingsdato"].asLocalDate()
        packet["@løsning"] =
            mapOf(
                "SenesteMuligeVirkningstidspunkt" to dagensDato.plusDays(14),
            )

        context.publish(packet.toJson())
    }
}
