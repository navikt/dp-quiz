package no.nav.dagpenger.quiz.mediator.behovløsere

import com.github.navikt.tbd_libs.rapids_and_rivers.JsonMessage
import com.github.navikt.tbd_libs.rapids_and_rivers.River
import com.github.navikt.tbd_libs.rapids_and_rivers.asLocalDate
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageContext
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageMetadata
import com.github.navikt.tbd_libs.rapids_and_rivers_api.RapidsConnection
import io.micrometer.core.instrument.MeterRegistry

internal class SenesteMuligeVirkningsdatoService(
    rapidsConnection: RapidsConnection,
) : River.PacketListener {
    init {
        River(rapidsConnection)
            .apply {
                precondition {
                    it.requireAllOrAny("@behov", listOf("SenesteMuligeVirkningstidspunkt"))
                    it.forbid("@løsning")
                }
                validate {
                    it.requireKey("Behandlingsdato")
                }
            }.register(this)
    }

    override fun onPacket(
        packet: JsonMessage,
        context: MessageContext,
        metadata: MessageMetadata,
        meterRegistry: MeterRegistry,
    ) {
        val dagensDato = packet["Behandlingsdato"].asLocalDate()
        packet["@løsning"] =
            mapOf(
                "SenesteMuligeVirkningstidspunkt" to dagensDato.plusDays(14),
            )

        context.publish(packet.toJson())
    }
}
