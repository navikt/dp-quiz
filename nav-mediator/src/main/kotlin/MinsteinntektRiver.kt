import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River
import java.util.UUID

internal class MinsteinntektRiver(
    rapidsConnection: RapidsConnection
) : River.PacketListener {
    init {
        River(rapidsConnection).apply {
            this.validate {
                it.requireKey("fnr", "søknadUuid", "seksjonsnavn")
                it.requireValue("seksjonsnavn", "minsteinntekt")
                it.forbid("@løsning")
                it.requireBesvartFakta("Verneplikt")
            }
        }.register(this)
    }

    private fun JsonMessage.faktaSvar(faktumNavn: String) =
        this["fakta"].toList().first { it["navn"].asText() == faktumNavn }["svar"]

    override fun onPacket(packet: JsonMessage, context: RapidsConnection.MessageContext) {
        val fnr = packet["fnr"].asText()
        val søknadUuid = packet["søknadUuid"]
        val virkningstidspunkt = packet.faktaSvar("Virkningstidspunkt")

        val packet = JsonMessage.newMessage(
            mutableMapOf(
                "@behov" to listOf("Minsteinntekt"),
                "@id" to UUID.randomUUID(),
                "fnr" to fnr,
                "beregningsdato" to virkningstidspunkt,
                "søknadUuid" to søknadUuid
            )
        )
        context.send(packet.toJson())
    }

    private fun JsonMessage.requireBesvartFakta(vararg faktum: String) {
        val f = faktum.asList()
        require("fakta") { fakta ->
            fakta.filter { it["navn"].asText() in f }.all { it.has("svar") }
        }
    }
}
