
import com.fasterxml.jackson.databind.JsonNode
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageProblems
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River
import no.nav.helse.rapids_rivers.asLocalDateTime
import java.util.UUID

internal abstract class HendelseRiver(
    rapidsConnection: RapidsConnection,
    private val messageMediator: MeldingMediator
) {
    protected val river = River(rapidsConnection)
    protected abstract val eventName: String
    protected abstract val riverName: String

    init {
        RiverImpl(river)
    }

    private fun validateHendelse(packet: JsonMessage) {
        packet.demandValue("@event_name", eventName)
        packet.require("@opprettet", JsonNode::asLocalDateTime)
        packet.require("@id") { UUID.fromString(it.asText()) }
    }

    protected abstract fun validate(packet: JsonMessage)
    protected abstract fun createMessage(packet: JsonMessage): HendelseMelding

    private inner class RiverImpl(river: River) : River.PacketListener {
        init {
            river.validate(::validateHendelse)
            river.validate(::validate)
            river.register(this)
        }

        override fun onPacket(packet: JsonMessage, context: RapidsConnection.MessageContext) {
            messageMediator.onRecognizedMessage(createMessage(packet), context)
        }

        override fun onError(problems: MessageProblems, context: RapidsConnection.MessageContext) {
            messageMediator.onRiverError(riverName, problems, context)
        }
    }
}
