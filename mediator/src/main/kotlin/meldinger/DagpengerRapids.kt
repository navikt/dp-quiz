package meldinger

import no.nav.helse.rapids_rivers.RapidsConnection

// Understands dagpenger messages
internal class DagpengerRapids(private val rapidsConnection: RapidsConnection) : RapidsConnection(), RapidsConnection.MessageListener {

    override fun publish(message: String) = rapidsConnection.publish(message)

    override fun publish(key: String, message: String) = rapidsConnection.publish(key, message)

    override fun start() = throw IllegalStateException()

    override fun stop() = throw IllegalStateException()

    override fun onMessage(message: String, context: MessageContext) {
        listeners.forEach { it.onMessage(message, context) }
    }
}
