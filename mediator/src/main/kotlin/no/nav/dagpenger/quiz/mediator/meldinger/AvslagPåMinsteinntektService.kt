package no.nav.dagpenger.quiz.mediator.meldinger

import mu.KotlinLogging
import no.nav.dagpenger.model.faktum.Dokument
import no.nav.dagpenger.model.faktum.Identer
import no.nav.dagpenger.model.seksjon.Prosesstype
import no.nav.dagpenger.quiz.mediator.db.ProsessRepository
import no.nav.dagpenger.quiz.mediator.soknad.Prosesser
import no.nav.dagpenger.quiz.mediator.soknad.avslagminsteinntekt.AvslagPåMinsteinntektOppsett.arenaFagsakId
import no.nav.dagpenger.quiz.mediator.soknad.avslagminsteinntekt.AvslagPåMinsteinntektOppsett.innsendtSøknadsId
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.MessageProblems
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River
import no.nav.helse.rapids_rivers.isMissingOrNull
import java.time.LocalDateTime
import java.util.UUID

internal class AvslagPåMinsteinntektService(
    private val prosessRepository: ProsessRepository,
    rapidsConnection: RapidsConnection,
    private val prosesstype: Prosesstype = Prosesser.AvslagPåMinsteinntekt,
) : River.PacketListener {
    private companion object {
        private val log = KotlinLogging.logger {}
        private val sikkerlogg = KotlinLogging.logger("tjenestekall.AvslagPåMinsteinntekt")
    }

    init {
        River(rapidsConnection).apply {
            validate {
                it.demandValue("@event_name", "innsending_ferdigstilt")
                it.demandValue("type", "NySøknad")
                it.requireKey("søknadsData.søknad_uuid")
                it.requireKey("fødselsnummer")
                it.requireKey("aktørId")
                it.requireKey("journalpostId")
                it.interestedIn("fagsakId")
            }
        }.register(this)
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) {
        val søknadsId = packet["søknadsData.søknad_uuid"].asText()
        val identer = Identer.Builder()
            .folkeregisterIdent(packet["fødselsnummer"].asText())
            .aktørId(packet["aktørId"].asText())
            .build()
        val journalpostId = packet["journalpostId"].asText()
        log.info { "Mottok søknad med id $søknadsId " }

        prosessRepository.ny(identer, prosesstype)
            .also { prosess ->
                // Arena-fagsakId for at arena-sink skal kunne lage vedtak på riktig sak
                val fagsakIdNode = packet["fagsakId"]
                if (!fagsakIdNode.isMissingOrNull()) {
                    prosess.dokument(arenaFagsakId).besvar(
                        Dokument(
                            lastOppTidsstempel = LocalDateTime.now(),
                            urn = "urn:fagsakid:${fagsakIdNode.asText()}",
                        ),
                    )
                }
                // Litt stygt, men så lenge vi leser fra innsendt søknad, så må vi lagre id-en for å hente ut data fra søknaden.
                prosess.dokument(innsendtSøknadsId).besvar(
                    Dokument(
                        lastOppTidsstempel = LocalDateTime.now(),
                        urn = "urn:soknadid:$søknadsId",
                    ),
                )

                prosessRepository.lagre(prosess)
                log.info { "Opprettet ny prosess ${prosess.fakta.uuid} på grunn av journalføring $journalpostId for søknad $søknadsId" }

                prosess.nesteSeksjoner()
                    .forEach { seksjon ->
                        context.publish(seksjon.somSpørsmål().also { sikkerlogg.debug { it } })
                        log.info { "Send seksjon ${seksjon.navn} for søknad ${prosess.fakta.uuid}" }
                    }
            }
    }

    override fun onError(problems: MessageProblems, context: MessageContext) {
        log.error { problems.toString() }
        sikkerlogg.error { problems.toExtendedReport() }
    }
}
