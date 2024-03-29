package no.nav.dagpenger.quiz.mediator.behovløsere

import no.nav.dagpenger.quiz.mediator.behovløsere.MinstearbeidsinntektFaktorStrategi.finnFaktor
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River
import no.nav.helse.rapids_rivers.asLocalDate

internal class TerskelFaktorService(rapidsConnection: RapidsConnection) :
    River.PacketListener {
    init {
        River(rapidsConnection).apply {
            validate { it.demandAll("@behov", listOf("ØvreTerskelFaktor", "NedreTerskelFaktor")) }
            validate { it.forbid("@løsning") }
            validate { it.requireKey("Virkningstidspunkt") }
        }.register(this)
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) {
        val terskler = finnFaktor(packet["Virkningstidspunkt"].asLocalDate())
        packet["@løsning"] = mapOf(
            "ØvreTerskelFaktor" to terskler.øvre,
            "NedreTerskelFaktor" to terskler.nedre
        )

        context.publish(packet.toJson())
    }
}
