package meldinger.model

import HendelseMediator
import no.nav.dagpenger.model.faktagrupper.Versjon
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.helse.rapids_rivers.JsonMessage
import java.util.UUID

internal class FaktumSvarMelding(packet: JsonMessage) : HendelseMelding(packet) {
    override val fødselsnummer = packet["fødselsnummer"].asText()
    private val søknadId = UUID.fromString(packet["søknadId"].asText())
    private val faktumId = packet["faktumId"].asInt()
    private val faktumType = packet["faktumType"].asText()
    private val faktagrupperType = Versjon.FaktagrupperType.valueOf(packet["faktagrupperType"].asText())
    private val rolle = Rolle.valueOf(packet["rolle"].asText())

    private val svar = when (faktumType) {
        "boolean" -> packet["svar"].asBoolean()
        else -> throw IllegalArgumentException("Ukjent faktum type: $faktumType")
    }

    override fun behandle(mediator: HendelseMediator) {
        mediator.behandle(this, søknadId, faktumId, svar, faktagrupperType, rolle)
    }
}
