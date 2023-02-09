package no.nav.dagpenger.quiz.mediator.meldinger

import mu.KotlinLogging
import mu.withLoggingContext
import no.nav.dagpenger.model.faktum.Identer
import no.nav.dagpenger.model.seksjon.Versjon
import no.nav.dagpenger.quiz.mediator.db.FaktaRecord
import no.nav.dagpenger.quiz.mediator.soknad.Prosess
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River
import java.util.UUID

internal class NyProsessBehovLøser(
    private val søknadPersistence: FaktaRecord,
    rapidsConnection: RapidsConnection,
) : River.PacketListener {
    private companion object {
        private val log = KotlinLogging.logger {}
        private val sikkerlogg = KotlinLogging.logger("tjenestekall")
    }

    init {
        River(rapidsConnection).apply {
            validate { it.demandValue("@event_name", "behov") }
            validate { it.demandAllOrAny("@behov", listOf("NySøknad")) }
            validate { it.requireKey("@id", "@opprettet") }
            validate { it.requireKey("søknad_uuid", "prosessnavn", "ident") }
            validate { it.forbid("@løsning") }
        }.register(this)
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) {
        val behovNavn = packet["@behov"].single().asText()
        val prosessnavn = packet["prosessnavn"].asText()
        val prosessversjon = when (prosessnavn) {
            "Dagpenger" -> Versjon.siste(Prosess.Dagpenger)
            "Innsending" -> Versjon.siste(Prosess.Innsending)
            else -> throw Error("Mangler prosess for $prosessnavn")
        }
        val søknadUuid = packet["søknad_uuid"].asText().let { søknadUuid -> UUID.fromString(søknadUuid) }

        withLoggingContext(
            "søknad_uuid" to søknadUuid.toString(),
        ) {
            log.info { "Mottok $behovNavn behov" }
            val identer = Identer.Builder()
                .folkeregisterIdent(packet["ident"].asText())
                // @todo: Aktør id?
                .build()

            søknadPersistence.ny(identer, prosessversjon, søknadUuid)
                .also { søknadsprosess ->
                    søknadPersistence.lagre(søknadsprosess.fakta)
                    log.info { "Opprettet ny søknadprosess ${søknadsprosess.fakta.uuid}" }

                    packet["@løsning"] = mapOf(
                        behovNavn to mapOf(
                            "prosessversjon" to mapOf(
                                "prosessnavn" to prosessversjon.faktatype.id,
                                "versjon" to prosessversjon.versjon,
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
