package no.nav.dagpenger.quiz.mediator.behovløsere

import mu.KotlinLogging
import mu.withLoggingContext
import no.nav.dagpenger.quiz.mediator.db.SøknadPersistence
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River

class MigrerProsessService(
    rapidsConnection: RapidsConnection,
    private val søknadPersistence: SøknadPersistence,
) : River.PacketListener {
    private companion object {
        val logger = KotlinLogging.logger { }
        val behov = "MigrerProsess"
    }

    init {
        River(rapidsConnection).apply {
            validate { it.demandValue("@event_name", "behov") }
            validate { it.demandAll("@behov", listOf(behov)) }
            validate { it.requireKey("søknad_uuid") }
            validate { it.forbid("@løsning") }
        }.register(this)
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) {
        val søknadId = packet.søknadUUID()

        withLoggingContext("søknadId" to søknadId.toString()) {

            val prosessversjon = søknadPersistence.migrer(søknadId)

            packet["@løsning"] = mapOf(
                behov to søknadId,
                "NyVersjon" to prosessversjon.versjon
            )

            context.publish(packet.toJson())
            logger.info { "Løser $behov" }
        }
    }
}
