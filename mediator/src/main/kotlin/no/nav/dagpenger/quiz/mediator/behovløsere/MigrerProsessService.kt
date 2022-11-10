package no.nav.dagpenger.quiz.mediator.behovløsere

import mu.KotlinLogging
import mu.withLoggingContext
import no.nav.dagpenger.model.marshalling.SøkerJsonBuilder
import no.nav.dagpenger.quiz.mediator.db.SøknadPersistence
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River

class MigrerProsessService(
    rapidsConnection: RapidsConnection,
    private val søknadPersistence: SøknadPersistence
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
            logger.info { "Løser $behov" }
            val prosessversjon = søknadPersistence.migrer(søknadId)
            val søknad = søknadPersistence.hent(søknadId)
            val søknadData = SøkerJsonBuilder(søknad).resultat().toString()

            packet["@løsning"] = mapOf(
                behov to mapOf(
                    "prosessnavn" to prosessversjon.prosessnavn,
                    "versjon" to prosessversjon.versjon,
                    "data" to søknadData
                )
            )

            context.publish(packet.toJson()).also {
                logger.info { "Publiserer løsning for $behov med prosessversjon=$prosessversjon" }
            }
        }
    }
}
