package no.nav.dagpenger.quiz.mediator.meldinger

import mu.KotlinLogging
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River

class ManuellBehandlingSink(
    rapidsConnection: RapidsConnection
) : River.PacketListener {

    companion object {
        private val log = KotlinLogging.logger {}
    }

    init {
        River(rapidsConnection).apply {
            validate {
                it.demandValue("@event_name", "manuell_behandling")
            }
        }.register(this)
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) {
        log.info { "Mottok melding om manuell behandling" }
    }
}
