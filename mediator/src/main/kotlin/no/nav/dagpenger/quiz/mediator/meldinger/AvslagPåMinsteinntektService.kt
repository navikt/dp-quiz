package no.nav.dagpenger.quiz.mediator.meldinger

import mu.KotlinLogging
import no.nav.dagpenger.model.faktum.Dokument
import no.nav.dagpenger.model.faktum.Identer
import no.nav.dagpenger.model.faktum.ProsessVersjon
import no.nav.dagpenger.model.seksjon.Versjon
import no.nav.dagpenger.quiz.mediator.db.SøknadPersistence
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.arenaFagsakId
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.innsendtSøknadsId
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.MessageProblems
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River
import no.nav.helse.rapids_rivers.isMissingOrNull
import java.time.LocalDateTime

internal class AvslagPåMinsteinntektService(
    private val søknadPersistence: SøknadPersistence,
    rapidsConnection: RapidsConnection,
    private val prosessVersjon: ProsessVersjon = AvslagPåMinsteinntektOppsett.VERSJON_ID
) : River.PacketListener {

    private companion object {
        private val log = KotlinLogging.logger {}
        private val sikkerlogg = KotlinLogging.logger("tjenestekall")
    }

    init {
        River(rapidsConnection).apply {
            validate {
                it.demandValue("@event_name", "innsending_ferdigstilt")
                it.demandValue("type", "NySøknad")
                it.requireKey("søknadsData.brukerBehandlingId")
                it.requireKey("fødselsnummer")
                it.requireKey("aktørId")
                it.requireKey("journalpostId")
                it.interestedIn("fagsakId")
            }
        }.register(this)
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) {

        log.info { "Mottok søknad med id ${packet["søknadsData.brukerBehandlingId"].asText()}" }
        val identer = Identer.Builder()
            .folkeregisterIdent(packet["fødselsnummer"].asText())
            .aktørId(packet["aktørId"].asText())
            .build()
        val søknadsId = packet["søknadsData.brukerBehandlingId"].asText()
        val faktagrupperType = Versjon.UserInterfaceType.Web
        val journalpostId = packet["journalpostId"].asText()

        søknadPersistence.ny(identer, faktagrupperType, prosessVersjon)
            .also { søknadprosess ->
                // Arena-fagsakId for at arena-sink skal kunne lage vedtak på riktig sak
                if (!packet["fagsakId"].isMissingOrNull()) {
                    søknadprosess.dokument(arenaFagsakId).besvar(Dokument(LocalDateTime.now(), url = packet["fagsakId"].asText()))
                }
                // Litt stygt, men så lenge vi leser fra innsendt søknad, så må vi lagre id-en for å hente ut data fra søknaden.
                søknadprosess.dokument(innsendtSøknadsId).besvar(Dokument(LocalDateTime.now(), url = søknadsId))

                søknadPersistence.lagre(søknadprosess.søknad)
                log.info { "Opprettet ny søknadprosess ${søknadprosess.søknad.uuid} på grunn av journalføring $journalpostId for søknad $søknadsId" }

                søknadprosess.nesteSeksjoner()
                    .forEach { seksjon ->
                        context.publish(seksjon.somSpørsmål().also { sikkerlogg.debug { it } })
                        log.info { "Send seksjon ${seksjon.navn} for søknad ${søknadprosess.søknad.uuid}" }
                    }
            }
    }

    override fun onError(problems: MessageProblems, context: MessageContext) {
        log.error { problems.toString() }
        sikkerlogg.error { problems.toExtendedReport() }
    }
}
