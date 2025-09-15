package no.nav.dagpenger.quiz.mediator.behovløsere

import com.github.navikt.tbd_libs.rapids_and_rivers.JsonMessage
import com.github.navikt.tbd_libs.rapids_and_rivers.River
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageContext
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageMetadata
import com.github.navikt.tbd_libs.rapids_and_rivers_api.RapidsConnection
import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.oshai.kotlinlogging.withLoggingContext
import io.micrometer.core.instrument.MeterRegistry
import no.nav.dagpenger.model.seksjon.Prosess
import no.nav.dagpenger.quiz.mediator.db.ProsessRepository

internal class MetadataService(
    rapidsConnection: RapidsConnection,
    private val utredningsRepository: ProsessRepository,
    private val metadataStrategi: MetadataStrategi,
) : River.PacketListener {
    private companion object {
        val logger = KotlinLogging.logger { }
        val behov = "InnsendingMetadata"
    }

    init {
        River(rapidsConnection)
            .apply {
                precondition {
                    it.requireValue("@event_name", "behov")
                    it.requireAllOrAny("@behov", listOf(behov))
                    it.forbid("@løsning")
                }
                validate {
                    it.requireKey("søknad_uuid")
                }
            }.register(this)
    }

    override fun onPacket(
        packet: JsonMessage,
        context: MessageContext,
        metadata: MessageMetadata,
        meterRegistry: MeterRegistry,
    ) {
        val søknadId = packet.søknadUUID()

        withLoggingContext("søknadId" to søknadId.toString()) {
            val metadata = metadataStrategi.metadata(utredningsRepository.hent(søknadId))
            packet["@løsning"] =
                mapOf(
                    behov to metadata,
                )

            context.publish(packet.toJson())
            logger.info { "Løser $behov med $metadata" }
        }
    }
}

fun interface MetadataStrategi {
    fun metadata(prosess: Prosess): Metadata

    data class Metadata(
        val skjemakode: String? = null,
        val tittel: String? = null,
    ) {
        init {
            require(listOf(skjemakode, tittel).any { it != null }) { "Metadata må ha enten skjemakode eller tittel" }
        }
    }
}
