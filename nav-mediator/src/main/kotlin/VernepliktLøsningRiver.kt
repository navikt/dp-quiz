import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River
import java.time.LocalDateTime
import java.util.UUID

internal class VernepliktLøsningRiver(
    rapidsConnection: RapidsConnection
) : River.PacketListener {
    init {
        River(rapidsConnection).apply {
            this.validate {
                it.requireKey("@løsning.Verneplikt", "søknadUuid")
            }
        }.register(this)
    }

    override fun onPacket(packet: JsonMessage, context: RapidsConnection.MessageContext): Unit =
        JsonMessage.newMessage(
            mutableMapOf(
                "@id" to UUID.randomUUID(),
                "@event_name" to "faktum_svar",
                "@opprettet" to LocalDateTime.now(),
                "søknadUuid" to packet["søknadUuid"],
                "faktumId" to 12,
                "svar" to packet["@løsning.Verneplikt"]["avtjentVerneplikt"].asBoolean()
            )
        ).toJson().let {
            context.send(it)
        }
}
