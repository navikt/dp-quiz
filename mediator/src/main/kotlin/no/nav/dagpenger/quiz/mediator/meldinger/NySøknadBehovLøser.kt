package no.nav.dagpenger.quiz.mediator.meldinger

import mu.KotlinLogging
import no.nav.dagpenger.model.faktum.Identer
import no.nav.dagpenger.model.faktum.Prosessversjon
import no.nav.dagpenger.model.marshalling.FaktaJsonBuilder
import no.nav.dagpenger.model.marshalling.NavJsonBuilder
import no.nav.dagpenger.model.seksjon.Versjon
import no.nav.dagpenger.quiz.mediator.db.SøknadRecord
import no.nav.dagpenger.quiz.mediator.soknad.Prosess
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.MessageProblems
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River
import no.nav.helse.rapids_rivers.withMDC
import java.util.UUID

internal class NySøknadBehovLøser(
    private val søknadPersistence: SøknadRecord,
    rapidsConnection: RapidsConnection,
    private val prosessVersjon: Prosessversjon = Versjon.siste(Prosess.Dagpenger)
) : River.PacketListener {

    private companion object {
        private val log = KotlinLogging.logger {}
        private val sikkerlogg = KotlinLogging.logger("tjenestekall")
    }

    private val behovNavn = "NySøknad"

    init {
        River(rapidsConnection).apply {
            validate { it.demandValue("@event_name", "behov") }
            validate { it.demandAllOrAny("@behov", listOf(behovNavn)) }
            validate { it.requireKey("@id", "@opprettet") }
            validate { it.requireKey("søknad_uuid") }
            validate { it.requireKey("ident") }
            validate { it.forbid("@løsning") }
        }.register(this)
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) {
        val søknadUuid = packet["søknad_uuid"].asText().let { søknadUuid -> UUID.fromString(søknadUuid) }
        withMDC("søknad_uuid" to søknadUuid.toString()) {
            log.info { "Mottok $behovNavn behov" }
            val identer = Identer.Builder()
                .folkeregisterIdent(packet["ident"].asText())
                // @todo: Aktør id?
                .build()

            val faktagrupperType = Versjon.UserInterfaceType.Web
            søknadPersistence.ny(identer, faktagrupperType, prosessVersjon, søknadUuid).also { søknadsprosess ->
                søknadPersistence.lagre(søknadsprosess.søknad)
                log.info { "Opprettet ny søknadprosess ${søknadsprosess.søknad.uuid}" }

                context.publish(
                    // TODO: Burde ikke være avhengige av navn på seksjonen her
                    NavJsonBuilder(søknadsprosess, "barnetillegg-register").resultat().toString().also {
                        sikkerlogg.info { "Behov sendt: $it" }
                    }
                )

                packet["@løsning"] = mapOf(behovNavn to søknadUuid)
                context.publish(packet.toJson())

                context.publish(
                    FaktaJsonBuilder(søknadsprosess).resultat().toString().also {
                        sikkerlogg.info { "Fakta sendt: $it" }
                    }
                )
            }
        }
    }

    override fun onError(problems: MessageProblems, context: MessageContext) {
        log.error { problems.toString() }
        sikkerlogg.error { problems.toExtendedReport() }
    }
}
