package no.nav.dagpenger.quiz.mediator.behovløsere

import mu.KotlinLogging
import mu.withLoggingContext
import no.nav.dagpenger.model.seksjon.Søknadprosess
import no.nav.dagpenger.quiz.mediator.db.SøknadPersistence
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River
import java.util.UUID

internal class SkjemakodeService(
    rapidsConnection: RapidsConnection,
    private val søknadPersistence: SøknadPersistence,
    private val skjemakodeStrategi: SkjemakodeStrategi
) : River.PacketListener {

    private companion object {
        val logger = KotlinLogging.logger { }
    }

    init {
        River(rapidsConnection).apply {
            validate { it.demandValue("@event_name", "behov") }
            validate { it.demandAll("@behov", listOf("Skjemakode")) }
            validate { it.requireKey("søknad_uuid") }
            validate { it.forbid("@løsning") }
        }.register(this)
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) {
        val søknadId = packet.søknadUUID()

        withLoggingContext("søknadId" to søknadId.toString()) {
            val skjemakode = skjemakodeStrategi.skjemakode(søknadPersistence.hent(søknadId))
            packet["@løsning"] = mapOf(
                "Skjemakode" to skjemakode
            )

            context.publish(packet.toJson())
            logger.info { "Løser behov for skjemakode" }
        }
    }
}

fun interface SkjemakodeStrategi {
    fun skjemakode(søknadprosess: Søknadprosess): Skjemakode
}

data class Skjemakode(val tittel: String, val skjemakode: String)

private fun JsonMessage.søknadUUID() = this["søknad_uuid"].asText().let { UUID.fromString(it) }
