package meldinger.model

import HendelseMediator
import no.nav.helse.rapids_rivers.JsonMessage

internal class ØnskerRettighetsavklaringMelding(packet: JsonMessage) : HendelseMelding(packet) {
    override val fødselsnummer = packet["fødselsnummer"].asText()

    private val ønskerRettighetsavklaring get() = ØnskerRettighetsavklaring()

    override fun behandle(mediator: HendelseMediator) {
        mediator.behandle(this, ønskerRettighetsavklaring)
    }
}
