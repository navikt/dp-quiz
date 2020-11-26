package no.nav.dagpenger.quiz.mediator.meldinger

import mu.KotlinLogging
import no.nav.dagpenger.model.seksjon.Versjon
import no.nav.dagpenger.quiz.mediator.db.SøknadPersistence
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River
import java.util.UUID

private val log = KotlinLogging.logger {}

internal class ArenaService(
    private val søknadPersistence: SøknadPersistence,
    rapidsConnection: RapidsConnection
) : River.PacketListener {
    private val river = River(rapidsConnection)
    private val eventName = "prosess_ferdig"
    private val arenaEventName = "arena_vedtak"

    init {
        river.validate(::validate)
        river.register(this)
    }

    private fun validate(packet: JsonMessage) {
        packet.demandValue("@event_name", eventName)
        packet.requireKey("søknad_uuid")
    }

    override fun onPacket(packet: JsonMessage, context: RapidsConnection.MessageContext) {
        val søknadUuid = UUID.fromString(packet["søknad_uuid"].asText())
        log.info { "Mottok prosess ferdig melding for $søknadUuid" }

        søknadPersistence.hent(søknadUuid, Versjon.UserInterfaceType.Web).also { søknadprosess ->
            context.send(
                JsonMessage.newMessage(
                    mapOf(
                        "@event_name" to arenaEventName,
                        "søknad_uuid" to søknadUuid
                    )
                ).toJson()
            )
        }
    }
}
