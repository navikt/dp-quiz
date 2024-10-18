package no.nav.dagpenger.quiz.mediator.behovløsere

import com.github.navikt.tbd_libs.rapids_and_rivers.JsonMessage
import com.github.navikt.tbd_libs.rapids_and_rivers.River
import com.github.navikt.tbd_libs.rapids_and_rivers.asLocalDate
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageContext
import com.github.navikt.tbd_libs.rapids_and_rivers_api.RapidsConnection
import no.nav.dagpenger.quiz.mediator.behovløsere.MinstearbeidsinntektFaktorStrategi.finnFaktor

internal class TerskelFaktorService(rapidsConnection: RapidsConnection) :
    River.PacketListener {
    init {
        River(rapidsConnection).apply {
            validate { it.demandAll("@behov", listOf("ØvreTerskelFaktor", "NedreTerskelFaktor")) }
            validate { it.forbid("@løsning") }
            validate { it.requireKey("Virkningstidspunkt") }
        }.register(this)
    }

    override fun onPacket(
        packet: JsonMessage,
        context: MessageContext,
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
