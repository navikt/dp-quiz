package meldinger.model

import HendelseMediator
import no.nav.dagpenger.model.faktum.Dokument
import no.nav.dagpenger.model.faktum.Inntekt.Companion.årlig
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.asLocalDate
import no.nav.helse.rapids_rivers.asLocalDateTime
import java.util.UUID

internal class FaktumSvarMelding(packet: JsonMessage) : HendelseMelding(packet) {
    override val fnr = packet["fnr"].asText()
    private val søknadUuid = UUID.fromString(packet["søknadUuid"].asText())
    private val faktumId = packet["faktumId"].asInt()
    private val clazz = packet["clazz"].asText()
    private val svar = packet["svar"]

    override fun behandle(mediator: HendelseMediator) {
        val typedSvar = when (clazz) {
            "boolean" -> svar.asBoolean()
            "heltall" -> svar.asInt()
            "dato" -> svar.asLocalDate()
            "inntekt" -> svar.asDouble().årlig
            "dokument" -> Dokument(svar["lastOppTidsstempel"].asLocalDateTime(), svar["url"].asText())
            else -> throw IllegalArgumentException("Ukjent faktum type: $clazz")
        }
        mediator.behandle(fnr, søknadUuid, faktumId, typedSvar)
    }
}
