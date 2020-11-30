package no.nav.dagpenger.quiz.mediator.meldinger

import com.fasterxml.jackson.databind.JsonNode
import mu.KotlinLogging
import no.nav.dagpenger.model.faktum.Dokument
import no.nav.dagpenger.model.faktum.Identer
import no.nav.dagpenger.model.seksjon.Versjon
import no.nav.dagpenger.quiz.mediator.Configuration
import no.nav.dagpenger.quiz.mediator.db.SøknadPersistence
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageProblems
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River
import no.nav.helse.rapids_rivers.asLocalDateTime
import java.time.LocalDateTime

private val log = KotlinLogging.logger {}
private val sikkerLogg = KotlinLogging.logger("tjenestekall")

internal class NySøknadService(
    private val søknadPersistence: SøknadPersistence,
    rapidsConnection: RapidsConnection
) : River.PacketListener {
    init {
        River(rapidsConnection).apply {
            validate {
                it.demandValue("@event_name", "Søknad")
                it.require("@opprettet", JsonNode::asLocalDateTime)
                it.requireKey("fnr", "aktørId", "søknadsId")
            }
        }.register(this)
    }

    override fun onPacket(packet: JsonMessage, context: RapidsConnection.MessageContext) {
        if (Configuration.prodEnvironment) return
        log.info { "Mottok ny søknadsmelding for ${packet["søknadsId"].asText()}" }

        val identer = Identer.Builder()
            .folkeregisterIdent(packet["fnr"].asText())
            .aktørId(packet["aktørId"].asText())
            .build()

        val søknadsId = packet["søknadsId"].asText()
        val faktagrupperType = Versjon.UserInterfaceType.Web
        søknadPersistence.ny(identer, faktagrupperType, Versjon.siste)
            .also { søknadprosess ->
                // TODO: Fikse dette
                søknadprosess.dokument(15).besvar(Dokument(LocalDateTime.now(), url = søknadsId))
                søknadPersistence.lagre(søknadprosess.søknad)

                søknadprosess.nesteSeksjoner()
                    .forEach { seksjon ->
                        context.send(seksjon.somSpørsmål())
                        log.info { "Send seksjon ${seksjon.navn} for søknad ${søknadprosess.søknad.uuid}, $søknadsId" }
                    }
            }
    }

    override fun onError(problems: MessageProblems, context: RapidsConnection.MessageContext) {
        log.error { problems.toString() }
        sikkerLogg.error { problems.toExtendedReport() }
    }
}
