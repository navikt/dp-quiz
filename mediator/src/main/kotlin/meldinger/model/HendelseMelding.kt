package meldinger.model
import HendelseMediator
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.asLocalDateTime
import java.util.UUID

internal abstract class HendelseMelding(private val packet: JsonMessage) {
    internal val id: UUID = UUID.fromString(packet["@id"].asText())
    internal val navn = packet["@event_name"].asText()
    internal val opprettet = packet["@opprettet"].asLocalDateTime()

    internal abstract val fødselsnummer: String

    internal abstract fun behandle(mediator: HendelseMediator)

    fun toJson() = packet.toJson()
}
