import db.HendelseRecorder
import mu.KotlinLogging
import mu.withLoggingContext
import no.nav.helse.rapids_rivers.MessageProblems
import no.nav.helse.rapids_rivers.RapidsConnection
import java.lang.IllegalStateException

private val log = KotlinLogging.logger {}
private val sikkerLogg = KotlinLogging.logger("tjenestekall")

internal class MeldingMediator(
    rapidsConnection: RapidsConnection,
    private val hendelseMediator: HendelseMediator,
    private val hendelseRecorder: HendelseRecorder
) {
    init {
        DelegatedRapid(rapidsConnection).also {

        }
    }

    private var messageRecognized = false
    private val riverSevereErrors = mutableListOf<Pair<String, MessageProblems>>()
    private val riverErrors = mutableListOf<Pair<String, MessageProblems>>()

    fun beforeRiverHandling(message: String) {
        messageRecognized = false
        riverSevereErrors.clear()
        riverErrors.clear()
    }

    fun onRecognizedMessage(melding: HendelseMelding, context: RapidsConnection.MessageContext){
        messageRecognized = true
        withLoggingContext(
            "melding_id" to melding.id.toString(),
            "melding_type" to (melding::class.simpleName?:"ukjent")) {
            sikkerLogg.info {  "gjenkjente melding for fnr=${melding.fødselsnummer}"}
            håndterMelding(melding, context)
        }
    }

    private fun håndterMelding(melding: HendelseMelding, context: RapidsConnection.MessageContext) {
        hendelseRecorder.lagreMelding(melding)
        melding.behandle(hendelseMediator)
    }

    fun afterRiverHandling(message: String) {
        if (messageRecognized) return
        if (riverErrors.isNotEmpty()) return sikkerLogg.warn(
            "kunne ikke gjenkjenne melding:\n\t$message\n\nProblemer:\n${
                riverErrors.joinToString(
                    separator = "\n"
                ) { "${it.first}:\n${it.second}" }
            }"
        )
        sikkerLogg.debug("ukjent melding:\n\t$message\n\nProblemer:\n${riverSevereErrors.joinToString(separator = "\n") { "${it.first}:\n${it.second}" }}")
    }

    private inner class DelegatedRapid(private val rapidsConnection: RapidsConnection) : RapidsConnection(),
        RapidsConnection.MessageListener {
        init {
            rapidsConnection.register(this)
        }

        override fun onMessage(message: String, context: MessageContext) {
            beforeRiverHandling(message)
            listeners.forEach { it.onMessage(message, context) }
            afterRiverHandling(message)
        }

        override fun publish(message: String) = rapidsConnection.publish(message)

        override fun publish(key: String, message: String) = rapidsConnection.publish(key, message)

        override fun start() = throw IllegalStateException()

        override fun stop() = throw IllegalStateException()
    }
}
