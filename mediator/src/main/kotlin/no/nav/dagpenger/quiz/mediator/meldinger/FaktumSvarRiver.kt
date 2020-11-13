package no.nav.dagpenger.quiz.mediator.meldinger

import no.nav.dagpenger.quiz.mediator.meldinger.model.FaktumSvarMelding
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.RapidsConnection

internal class FaktumSvarRiver(rapidsConnection: RapidsConnection, mediator: MeldingMediator) :
    HendelseRiver(rapidsConnection, mediator) {

    override val eventName = "faktum_svar"
    override val riverName = "Faktum svar"

    override fun validate(packet: JsonMessage) {
        packet.requireKey(
            "fnr",
            "opprettet",
            "faktumId",
            "svar",
            "søknadUuid",
            "clazz",
        )
    }

    override fun createMessage(packet: JsonMessage) = FaktumSvarMelding(packet)
}
