package no.nav.dagpenger.quiz.mediator.meldinger

import mu.KotlinLogging
import no.nav.dagpenger.model.faktum.Identer
import no.nav.dagpenger.model.faktum.Prosessversjon
import no.nav.dagpenger.model.seksjon.Versjon
import no.nav.dagpenger.quiz.mediator.db.SøknadRecord
import no.nav.dagpenger.quiz.mediator.soknad.Dagpenger
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River
import java.util.UUID

internal class DagpengerService(
    private val søknadPersistence: SøknadRecord,
    rapidsConnection: RapidsConnection,
    private val prosessVersjon: Prosessversjon = Dagpenger.VERSJON_ID
) : River.PacketListener {

    private companion object {
        private val log = KotlinLogging.logger {}
        private val sikkerlogg = KotlinLogging.logger("tjenestekall")
    }

    init {
        River(rapidsConnection).apply {
            validate {
                it.demandValue("@event_name", "ønsker_rettighetsavklaring")
                it.requireKey("@id", "@opprettet")
                it.requireKey("søknad_uuid")
                it.requireKey("fødselsnummer")
            }
        }.register(this)
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) {
        log.info { "Mottok ønsker rettighetsavklaring med id ${packet["@id"].asText()}" }
        val identer = Identer.Builder()
            .folkeregisterIdent(packet["fødselsnummer"].asText())
            // @todo: Aktør id?
            .build()

        val søknadUuid = packet["søknad_uuid"].asText().let { søknadUuid -> UUID.fromString(søknadUuid) }
        val faktagrupperType = Versjon.UserInterfaceType.Web
        søknadPersistence.ny(identer, faktagrupperType, prosessVersjon, søknadUuid).also { søknadsprosess ->
            søknadPersistence.lagre(søknadsprosess.søknad)
            log.info { "Opprettet ny søknadprosess ${søknadsprosess.søknad.uuid} på grunn av ønsket rettighetsavklaring" }
            søknadsprosess.nesteSeksjoner()
                .forEach { seksjon ->
                    context.publish(seksjon.somSpørsmål().also { sikkerlogg.debug { it } })
                    log.info { "Send seksjon ${seksjon.navn} for søknad ${søknadsprosess.søknad.uuid}" }
                }
        }
    }
}
