package no.nav.dagpenger.quiz.mediator.behovløsere

import com.github.navikt.tbd_libs.rapids_and_rivers.JsonMessage
import com.github.navikt.tbd_libs.rapids_and_rivers.River
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageContext
import com.github.navikt.tbd_libs.rapids_and_rivers_api.RapidsConnection
import mu.KotlinLogging
import mu.withLoggingContext
import no.nav.dagpenger.model.marshalling.SøkerJsonBuilder
import no.nav.dagpenger.quiz.mediator.db.FaktaRepository
import no.nav.dagpenger.quiz.mediator.db.ProsessRepository

class MigrerProsessService(
    rapidsConnection: RapidsConnection,
    private val faktaRepository: FaktaRepository,
    private val prosessRepository: ProsessRepository,
) : River.PacketListener {
    private companion object {
        val logger = KotlinLogging.logger { }
        const val BEHOV = "MigrerProsess"
    }

    init {
        River(rapidsConnection).apply {
            validate { it.demandValue("@event_name", "behov") }
            validate { it.demandAll("@behov", listOf(BEHOV)) }
            validate { it.requireKey("søknad_uuid") }
            validate { it.forbid("@løsning") }
        }.register(this)
    }

    override fun onPacket(
        packet: JsonMessage,
        context: MessageContext,
    ) {
        val søknadId = packet.søknadUUID()

        withLoggingContext("søknadId" to søknadId.toString()) {
            logger.info { "Løser $BEHOV" }

            if (!faktaRepository.eksisterer(søknadId)) {
                logger.warn { "Migrering av søknadId=$søknadId kunne ikke migreres siden den ikke eksisterer" }
                return
            }
            val prosessversjon = faktaRepository.migrer(søknadId)
            val søknad = prosessRepository.hent(søknadId)
            val søknadData = SøkerJsonBuilder(søknad).resultat().toString()

            packet["@løsning"] =
                mapOf(
                    BEHOV to
                        mapOf(
                            "prosessnavn" to prosessversjon.faktatype,
                            "versjon" to prosessversjon.versjon,
                            "data" to søknadData,
                        ),
                )

            context.publish(packet.toJson()).also {
                logger.info { "Publiserer løsning for $BEHOV med prosessversjon=$prosessversjon" }
            }
        }
    }
}
