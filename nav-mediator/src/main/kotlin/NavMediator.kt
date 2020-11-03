import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River

class NavMediator(private val rapidsConnection: RapidsConnection) : River.PacketListener {

    init {
        River(rapidsConnection).apply {
            validate { it.requireKey("@behov") }
            validate { it.demandValue("@event_name", "behov") }
            validate { it.rejectKey("@løsning") }
            validate { it.requireKey("@id", "@opprettet") }
            validate { it.requireKey("fødselsnummer", "fakta", "søknadId") }
        }.register(this)
    }

    override fun onPacket(packet: JsonMessage, context: RapidsConnection.MessageContext) {

        val behov = packet["@behov"].map { it.asText() }

        // For hvert behov i fakta ->
        // Gitt faktum SisteInntektår  - avhenger virkningstidspunkt og om søker er fra fangst og fiske
        // hent inntekt siste 36 måneder (virkningstidspunkt = dato)
        // summer inntekt på siste år (basert inntektsklasser? )
        // svar SisteInntektår med inntekt

        context.send(
            JsonMessage.newMessage(
                mapOf(
                    "faktum" to "InntektSiste3år",
                    "id" to "1123",
                    "svar" to "10000"
                )
            ).toJson()
        )
    }
}
