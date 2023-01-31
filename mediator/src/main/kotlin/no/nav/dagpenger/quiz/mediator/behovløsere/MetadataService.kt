package no.nav.dagpenger.quiz.mediator.behovløsere

import mu.KotlinLogging
import mu.withLoggingContext
import no.nav.dagpenger.model.seksjon.Faktagrupper
import no.nav.dagpenger.quiz.mediator.db.SøknadPersistence
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River

internal class MetadataService(
    rapidsConnection: RapidsConnection,
    private val søknadPersistence: SøknadPersistence,
    private val metadataStrategi: MetadataStrategi
) : River.PacketListener {
    private companion object {
        val logger = KotlinLogging.logger { }
        val behov = "InnsendingMetadata"
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
            val metadata = metadataStrategi.metadata(søknadPersistence.hent(søknadId))
            packet["@løsning"] = mapOf(
                behov to metadata
            )

            context.publish(packet.toJson())
            logger.info { "Løser $behov med $metadata" }
        }
    }
}

fun interface MetadataStrategi {
    fun metadata(faktagrupper: Faktagrupper): Metadata

    data class Metadata(val skjemakode: String? = null, val tittel: String? = null) {
        init {
            require(listOf(skjemakode, tittel).any { it != null }) { "Metadata må ha enten skjemakode eller tittel" }
        }
    }
}
