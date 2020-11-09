
import no.nav.helse.rapids_rivers.JsonMessage

internal class MinsteinntektBehovMelding(packet: JsonMessage) : HendelseMelding(packet) {
    override val fnr = packet["fnr"].asText()
    val søknadUuid = packet["søknadUuid"]
    val virkningstidspunkt = packet.faktaSvar("Virkningstidspunkt")

    override fun behandle(mediator: HendelseMediator) {
        val packet = JsonMessage.newMessage(
            mutableMapOf(
                "@behov" to listOf("Minsteinntekt"),
                "@id" to "12345",
                "fnr" to fnr,
                "beregningsdato" to virkningstidspunkt,
                "søknadUuid" to søknadUuid
            )
        )
        mediator.behandle(packet)
    }
}

private fun JsonMessage.faktaSvar(faktumNavn: String) =
    this["fakta"].toList().first { it["navn"].asText() == faktumNavn }["svar"]
