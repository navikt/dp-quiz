package meldinger

import MeldingMediator
import meldinger.model.ØnskerRettighetsavklaringMelding
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.RapidsConnection

internal class ØnskerRettighetsavklaringerRiver(rapidsConnection: RapidsConnection, mediator: MeldingMediator) :
    HendelseRiver(rapidsConnection, mediator) {

    override val eventName = "ønsker_rettighetsavklaring"
    override val riverName = "Ønsker rettighetsavklaring"

    override fun validate(packet: JsonMessage) {
        packet.requireKey("fnr", "opprettet", "faktagrupperType")
    }

    override fun createMessage(packet: JsonMessage) = ØnskerRettighetsavklaringMelding(packet)
}
