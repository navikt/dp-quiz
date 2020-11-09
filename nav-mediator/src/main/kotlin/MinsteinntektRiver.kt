import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.RapidsConnection

internal class MinsteinntektRiver(
    rapidsConnection: RapidsConnection,
    meldingMediator: MeldingMediator,
) : HendelseRiver(rapidsConnection, meldingMediator) {
    override val eventName = "behov"
    override val riverName = "Behov"
    override fun validate(packet: JsonMessage) {
        packet.requireKey("fnr", "søknadUuid", "seksjonsnavn")
        packet.forbid("@løsning")
        packet.requireValue("seksjonsnavn", "minsteinntekt")
        packet.requireBesvartFakta("Verneplikt")
    }

    override fun createMessage(packet: JsonMessage) = MinsteinntektBehovMelding(packet)
}

private fun JsonMessage.requireBesvartFakta(vararg faktum: String) {
    val f = faktum.asList()
    require("fakta") { fakta ->
        fakta.filter { it["navn"].asText() in f }.all { it.has("svar") }
    }
}
