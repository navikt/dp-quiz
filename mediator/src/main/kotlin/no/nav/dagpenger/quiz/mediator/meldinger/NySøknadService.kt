package no.nav.dagpenger.quiz.mediator.meldinger

import com.fasterxml.jackson.databind.JsonNode
import mu.KotlinLogging
import no.nav.dagpenger.model.seksjon.Versjon
import no.nav.dagpenger.quiz.mediator.Configuration
import no.nav.dagpenger.quiz.mediator.db.SøknadPersistence
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River
import no.nav.helse.rapids_rivers.asLocalDateTime

private val log = KotlinLogging.logger {}

internal class NySøknadService(
    private val søknadPersistence: SøknadPersistence,
    rapidsConnection: RapidsConnection
) : River.PacketListener {
    init {
        River(rapidsConnection).apply {
            validate {
                it.demandValue("@event_name", "Søknad")
                it.require("@opprettet", JsonNode::asLocalDateTime)
                it.requireKey("fødselsnummer", "aktørId", "søknadsId")
            }
        }.register(this)
    }

    override fun onPacket(packet: JsonMessage, context: RapidsConnection.MessageContext) {
        if (Configuration.prodEnvironment) return
        log.info { "Mottok ny søknadsmelding for ${packet["søknadsId"].asText()}" }

        val fnr = packet["fødselsnummer"].asText()
        val faktagrupperType = Versjon.UserInterfaceType.Web
        søknadPersistence.ny(fnr, faktagrupperType, Versjon.siste)
            .also { søknadprosess ->
                søknadprosess.nesteSeksjoner()
                    .forEach { seksjon ->
                        context.send(seksjon.somSpørsmål())
                    }
            }
    }
}
