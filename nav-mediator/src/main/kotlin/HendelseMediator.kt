import mu.KotlinLogging
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.RapidsConnection

private val log = KotlinLogging.logger {}

internal class HendelseMediator(private val rapidsConnection: RapidsConnection) {

    fun behandle(packet: JsonMessage) {
        rapidsConnection.publish(packet.toJson())
    }
}
