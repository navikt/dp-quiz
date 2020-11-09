import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River
import java.util.UUID

internal class VernepliktRiver(
    rapidsConnection: RapidsConnection
) : River.PacketListener {
    init {
        River(rapidsConnection).apply {
            this.validate {
                it.requireKey("fnr", "søknadUuid", "seksjonsnavn")
                it.requireValue("seksjonsnavn", "verneplikt")
                it.forbid("@løsning")
            }
        }.register(this)
    }

    override fun onPacket(packet: JsonMessage, context: RapidsConnection.MessageContext) {
        val fnr = packet["fnr"].asText()
        val søknadUuid = packet["søknadUuid"]

        val packet = JsonMessage.newMessage(
            mutableMapOf(
                "@behov" to listOf("Verneplikt"),
                "@id" to UUID.randomUUID(),
                "fnr" to fnr,
                "søknadUuid" to søknadUuid
            )
        )
        context.send(packet.toJson())
    }
}
