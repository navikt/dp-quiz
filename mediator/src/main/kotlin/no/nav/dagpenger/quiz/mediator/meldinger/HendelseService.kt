package no.nav.dagpenger.quiz.mediator.meldinger

import com.fasterxml.jackson.databind.JsonNode
import mu.KotlinLogging
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageProblems
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River
import no.nav.helse.rapids_rivers.asLocalDateTime
import java.util.UUID

private val log = KotlinLogging.logger {}
private val sikkerLogg = KotlinLogging.logger("tjenestekall")

internal abstract class HendelseService(rapidsConnection: RapidsConnection) : River.PacketListener {
    private val river = River(rapidsConnection)
    protected abstract val eventName: String
    protected abstract val riverName: String

    init {
        river.validate(::validateHendelse)
        river.validate(::validate)
        river.register(this)
    }

    private fun validateHendelse(packet: JsonMessage) {
        packet.demandValue("@event_name", eventName)
        packet.require("@opprettet", JsonNode::asLocalDateTime)
        packet.require("@id") { UUID.fromString(it.asText()) }
    }

    protected abstract fun validate(packet: JsonMessage)

    override fun onError(problems: MessageProblems, context: RapidsConnection.MessageContext) {
        sikkerLogg.error("$riverName, $problems")
    }
}
