package no.nav.dagpenger.quiz.mediator.meldinger

import mu.KotlinLogging
import no.nav.dagpenger.model.faktum.Dokument
import no.nav.dagpenger.model.faktum.Identer
import no.nav.dagpenger.model.seksjon.Versjon
import no.nav.dagpenger.quiz.mediator.db.SøknadPersistence
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.innsendtSøknadsId
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.MessageProblems
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River
import java.time.LocalDateTime

private val log = KotlinLogging.logger {}
private val sikkerLogg = KotlinLogging.logger("tjenestekall")

internal class NySøknadService(
    private val søknadPersistence: SøknadPersistence,
    rapidsConnection: RapidsConnection,
    private val versjonId: Int = Versjon.siste
) : River.PacketListener {
    init {
        River(rapidsConnection).apply {
            validate {
                it.demandValue("@event_name", "Søknad")
                it.requireKey("fnr", "aktørId", "søknadsId")
            }
        }.register(this)
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) {
        log.info { "Mottok ny søknadsmelding for ${packet["søknadsId"].asText()}" }
        val identer = Identer.Builder()
            .folkeregisterIdent(packet["fnr"].asText())
            .aktørId(packet["aktørId"].asText())
            .build()
        val søknadsId = packet["søknadsId"].asText()
        val faktagrupperType = Versjon.UserInterfaceType.Web
        søknadPersistence.ny(identer, faktagrupperType, versjonId)
            .also { søknadprosess ->
                // TODO: Fikse dette
                søknadprosess.dokument(innsendtSøknadsId).besvar(Dokument(LocalDateTime.now(), url = søknadsId))
                søknadPersistence.lagre(søknadprosess.søknad)

                log.info { "Opprettet ny søknadprosess ${søknadprosess.søknad.uuid} på grunn av søknad $søknadsId" }

                søknadprosess.nesteSeksjoner()
                    .forEach { seksjon ->
                        context.publish(seksjon.somSpørsmål().also { sikkerLogg.debug { it } })
                        log.info { "Send seksjon ${seksjon.navn} for søknad ${søknadprosess.søknad.uuid}" }
                    }
            }
    }

    override fun onError(problems: MessageProblems, context: MessageContext) {
        log.error { problems.toString() }
        sikkerLogg.error { problems.toExtendedReport() }
    }
}
