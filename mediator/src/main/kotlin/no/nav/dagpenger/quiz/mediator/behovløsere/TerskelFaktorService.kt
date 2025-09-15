package no.nav.dagpenger.quiz.mediator.behovløsere

import com.github.navikt.tbd_libs.rapids_and_rivers.JsonMessage
import com.github.navikt.tbd_libs.rapids_and_rivers.River
import com.github.navikt.tbd_libs.rapids_and_rivers.asLocalDate
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageContext
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageMetadata
import com.github.navikt.tbd_libs.rapids_and_rivers_api.RapidsConnection
import io.micrometer.core.instrument.MeterRegistry
import no.nav.dagpenger.quiz.mediator.behovløsere.MinstearbeidsinntektFaktorStrategi.finnFaktor

internal class TerskelFaktorService(
    rapidsConnection: RapidsConnection,
) : River.PacketListener {
    init {
        River(rapidsConnection)
            .apply {
                precondition {
                    it.requireAll("@behov", listOf("ØvreTerskelFaktor", "NedreTerskelFaktor"))
                    it.forbid("@løsning")
                }
                validate {
                    it.requireKey("Virkningstidspunkt")
                }
            }.register(this)
    }

    override fun onPacket(
        packet: JsonMessage,
        context: MessageContext,
        metadata: MessageMetadata,
        meterRegistry: MeterRegistry,
    ) {
        val terskler = finnFaktor(packet["Virkningstidspunkt"].asLocalDate())
        packet["@løsning"] =
            mapOf(
                "ØvreTerskelFaktor" to terskler.øvre,
                "NedreTerskelFaktor" to terskler.nedre,
            )

        context.publish(packet.toJson())
    }
}
