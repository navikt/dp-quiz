package no.nav.dagpenger.quiz.mediator.behovløsere

import mu.KotlinLogging
import mu.withLoggingContext
import no.nav.dagpenger.model.faktum.Dokument
import no.nav.dagpenger.quiz.mediator.db.SøknadPersistence
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River
import no.nav.helse.rapids_rivers.asLocalDateTime

class DokumentkravSvarService(
    rapidsConnection: RapidsConnection,
    private val søknadPersistence: SøknadPersistence
) : River.PacketListener {
    private companion object {
        val logger = KotlinLogging.logger { }
        val behov = "DokumentkravSvar"
    }

    init {
        River(rapidsConnection).apply {
            validate { it.demandValue("@event_name", "behov") }
            validate { it.demandAll("@behov", listOf(behov)) }
            validate { it.requireKey("søknad_uuid", behov) }
            validate { it.forbid("@løsning") }
        }.register(this)
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) {
        val søknadId = packet.søknadUUID()
        withLoggingContext("søknadId" to søknadId.toString()) {
            try {
                val faktumId = packet.faktumId()
                val svar = packet.dokumentsvar()
                søknadPersistence.hent(søknadId).let { søknadprosess ->
                    søknadprosess.dokument(faktumId).besvar(svar)
                    søknadPersistence.lagre(søknadprosess.fakta)
                }

                packet["@løsning"] = mapOf(
                    behov to søknadId
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
