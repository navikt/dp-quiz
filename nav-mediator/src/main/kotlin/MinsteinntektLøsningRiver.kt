import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River
import java.time.LocalDateTime
import java.util.UUID

internal class MinsteinntektLøsningRiver(
    rapidsConnection: RapidsConnection
) : River.PacketListener {
    init {
        River(rapidsConnection).apply {
            this.validate {
                it.requireKey("@løsning.Minsteinntekt", "søknadUuid")
            }
        }.register(this)
    }

    override fun onPacket(packet: JsonMessage, context: RapidsConnection.MessageContext) {
        context.send(createSvarPacket(2, "inntektSiste1År", packet))
        context.send(createSvarPacket(3, "inntektSiste3År", packet))
    }

    private fun createSvarPacket(faktumId: Int, svarKey: String, packet: JsonMessage): String {
        val søknadUuid = packet["søknadUuid"]
        val svar = packet["@løsning.Minsteinntekt"]

        return JsonMessage.newMessage(
            mutableMapOf(
                "@id" to UUID.randomUUID(),
                "@event_name" to "faktum_svar",
                "@opprettet" to LocalDateTime.now(),
                "søknadUuid" to søknadUuid,
                "faktumId" to faktumId,
                "svar" to svar["$svarKey"].asDouble()
            )
        ).toJson()
    }
}
