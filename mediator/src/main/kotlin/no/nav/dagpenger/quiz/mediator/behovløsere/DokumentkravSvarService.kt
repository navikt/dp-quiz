package no.nav.dagpenger.quiz.mediator.behovløsere

import com.github.navikt.tbd_libs.rapids_and_rivers.JsonMessage
import com.github.navikt.tbd_libs.rapids_and_rivers.River
import com.github.navikt.tbd_libs.rapids_and_rivers.asLocalDateTime
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageContext
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageMetadata
import com.github.navikt.tbd_libs.rapids_and_rivers_api.RapidsConnection
import io.micrometer.core.instrument.MeterRegistry
import mu.KotlinLogging
import mu.withLoggingContext
import no.nav.dagpenger.model.faktum.Dokument
import no.nav.dagpenger.quiz.mediator.db.ProsessRepository

class DokumentkravSvarService(
    rapidsConnection: RapidsConnection,
    private val prosessRepository: ProsessRepository,
) : River.PacketListener {
    private companion object {
        val logger = KotlinLogging.logger { }
        val behov = "DokumentkravSvar"
    }

    init {
        River(rapidsConnection).apply {
            precondition {
                it.requireValue("@event_name", "behov")
                it.requireAllOrAny("@behov", listOf(behov))
                it.forbid("@løsning")
            }
            validate {
                it.requireKey("søknad_uuid", behov)
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
            try {
                val faktumId = packet.faktumId()
                val svar = packet.dokumentsvar()
                prosessRepository.hent(søknadId).let { søknadprosess ->
                    søknadprosess.dokument(faktumId).besvar(svar)
                    prosessRepository.lagre(søknadprosess)
                }

                packet["@løsning"] =
                    mapOf(
                        behov to søknadId,
                    )

                context.publish(packet.toJson())
                logger.info { "Løser $behov" }
            } catch (e: java.lang.IllegalArgumentException) {
                logger.error(e) { "Hopper over pakker med gammel prosessversjon" }
            }
        }
    }

    private fun JsonMessage.faktumId() = this[behov]["id"].asText()

    private fun JsonMessage.dokumentsvar(): Dokument {
        val lastOppTidsstempel = this[behov]["lastOppTidsstempel"].asLocalDateTime()
        val urn = this[behov]["urn"].asText()
        return Dokument(lastOppTidsstempel, urn)
    }
}
