package meldinger.model

import HendelseMediator
import no.nav.dagpenger.model.faktagrupper.Versjon
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.asLocalDate
import java.util.UUID

internal class FaktumSvarMelding(packet: JsonMessage) : HendelseMelding(packet) {
    override val fødselsnummer = packet["fødselsnummer"].asText()
    private val søknadId = UUID.fromString(packet["søknadId"].asText())
    private val faktumId = packet["faktumId"].asInt()
    private val clazz = packet["clazz"].asText()
    private val faktagrupperType = Versjon.FaktagrupperType.valueOf(packet["faktagrupperType"].asText())
    private val rolle = Rolle.valueOf(packet["rolle"].asText())
    private val svar = packet["svar"]

    override fun behandle(mediator: HendelseMediator) {
        val typedSvar = when (clazz) {
            "boolean" -> svar.asBoolean()
            "heltall" -> svar.asInt()
            "dato" -> svar.asLocalDate()
            else -> throw IllegalArgumentException("Ukjent faktum type: $clazz")
        }
        mediator.behandle(this, søknadId, faktumId, typedSvar, faktagrupperType, rolle)
    }
}
