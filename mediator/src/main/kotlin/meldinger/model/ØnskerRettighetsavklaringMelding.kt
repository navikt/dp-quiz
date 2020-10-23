package meldinger.model

import HendelseMediator
import no.nav.helse.rapids_rivers.JsonMessage

// Forstår en JSON message som representerer
internal class ØnskerRettighetsavklaringMelding(packet: JsonMessage) : HendelseMelding(packet) {
    override val fødselsnummer = packet["fødselsnummer"].asText()

    override fun behandle(mediator: HendelseMediator) {
        mediator.behandle(this, fødselsnummer)
    }
}
