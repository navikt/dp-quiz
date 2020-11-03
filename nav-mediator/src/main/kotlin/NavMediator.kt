import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River

class NavMediator(private val rapidsConnection: RapidsConnection): River.PacketListener {

    init {
        River(rapidsConnection).apply {
            validate { it.demandAll("@behov", listOf("Inntekt")) }
            validate { it.demandValue("@event_name", "behov") }
            validate { it.rejectKey("@løsning") }
            validate { it.requireKey("@id", "@opprettet") }
            validate { it.requireKey("fødselsnummer", "fakta", "søknadId") }
        }.register(this)
    }

    override fun onPacket(packet: JsonMessage, context: RapidsConnection.MessageContext) {
        context.send(packet.toJson())
    }

}