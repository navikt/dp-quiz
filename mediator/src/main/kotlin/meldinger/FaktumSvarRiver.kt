package meldinger

import MeldingMediator
import meldinger.model.FaktumSvarMelding
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.RapidsConnection

internal class FaktumSvarRiver(rapidsConnection: RapidsConnection, mediator: MeldingMediator) :
    HendelseRiver(rapidsConnection, mediator) {

    override val eventName = "faktum_svar"
    override val riverName = "Faktum svar"

    override fun validate(packet: JsonMessage) {
        packet.requireKey(
            "fødselsnummer",
            "opprettet",
            "faktumId",
            "svar",
            "søknadId",
            "faktumType",
            "rolle",
            "faktagrupperType"
        )
    }

    override fun createMessage(packet: JsonMessage) = FaktumSvarMelding(packet)
}
