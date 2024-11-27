package no.nav.dagpenger.quiz.mediator.meldinger

import com.github.navikt.tbd_libs.rapids_and_rivers.JsonMessage
import com.github.navikt.tbd_libs.rapids_and_rivers.River
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageContext
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageMetadata
import com.github.navikt.tbd_libs.rapids_and_rivers_api.RapidsConnection
import io.micrometer.core.instrument.MeterRegistry
import mu.KotlinLogging
import mu.withLoggingContext
import no.nav.dagpenger.model.faktum.Identer
import no.nav.dagpenger.model.seksjon.Henvendelser
import no.nav.dagpenger.quiz.mediator.db.ProsessRepository
import no.nav.dagpenger.quiz.mediator.soknad.Prosesser
import java.util.UUID

internal class NyProsessBehovLøser(
    private val prosessRepository: ProsessRepository,
    rapidsConnection: RapidsConnection,
) : River.PacketListener {
    private companion object {
        private val log = KotlinLogging.logger {}
        private val sikkerlogg = KotlinLogging.logger("tjenestekall")
    }

    init {
        River(rapidsConnection).apply {
            precondition {
                it.requireValue("@event_name", "behov")
                it.requireAllOrAny("@behov", listOf("NySøknad"))
            }

            validate {
                it.requireKey("@id", "@opprettet")
                it.requireKey("søknad_uuid", "prosessnavn", "ident")
                it.forbid("@løsning")
            }
        }.register(this)
    }

    override fun onPacket(
        packet: JsonMessage,
        context: MessageContext,
        metadata: MessageMetadata,
        meterRegistry: MeterRegistry,
    ) {
        val behovNavn = packet["@behov"].single().asText()
        val prosessnavn = packet.get("prosessnavn").asText()
        val prosessversjon =
            when (prosessnavn) {
                "Dagpenger" -> Prosesser.Søknad
                "Innsending" -> Prosesser.Innsending
                else -> throw Error("Mangler prosess for $prosessnavn")
            }
        val søknadUuid = packet["søknad_uuid"].asText().let { søknadUuid -> UUID.fromString(søknadUuid) }

        withLoggingContext(
            "søknad_uuid" to søknadUuid.toString(),
        ) {
            log.info { "Mottok $behovNavn behov" }
            val identer =
                Identer.Builder()
                    .folkeregisterIdent(packet["ident"].asText())
                    .build()

            prosessRepository.ny(identer, prosessversjon, søknadUuid)
                .also { søknadsprosess ->
                    prosessRepository.lagre(søknadsprosess)
                    log.info { "Opprettet ny søknadprosess ${søknadsprosess.fakta.uuid}" }
                    val faktaversjon = Henvendelser.siste(prosessversjon.faktatype)

                    packet["@løsning"] =
                        mapOf(
                            behovNavn to
                                mapOf(
                                    "prosessversjon" to
                                        mapOf(
                                            "prosessnavn" to prosessversjon.faktatype.id,
                                            // TODO: Denne trengs ikke om vi slutter med migrering fra dp-soknad
                                            "versjon" to faktaversjon.versjon,
                                        ),
                                ),
                        )
                    context.publish(
                        packet.toJson().also {
                            log.info { "Publiserer løsning med prosessversjon=$prosessversjon" }
                        },
                    )

                    søknadsprosess.sendNesteSeksjon(context)
                }
        }
    }
}
