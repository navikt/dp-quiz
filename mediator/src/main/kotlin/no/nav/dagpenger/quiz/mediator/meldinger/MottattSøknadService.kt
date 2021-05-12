package no.nav.dagpenger.quiz.mediator.meldinger

import mu.KotlinLogging
import no.nav.dagpenger.model.faktum.Identer
import no.nav.dagpenger.model.seksjon.Versjon
import no.nav.dagpenger.quiz.mediator.db.SøknadPersistence
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.MessageProblems
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River

private val log = KotlinLogging.logger {}
private val sikkerLogg = KotlinLogging.logger("tjenestekall")

internal class MottattSøknadService(
    private val søknadPersistence: SøknadPersistence,
    rapidsConnection: RapidsConnection,
    private val versjonId: Int = Versjon.siste
) : River.PacketListener {
    init {
        River(rapidsConnection).apply {
            validate {
                it.demandValue("@event_name", "innsending_ferdigstilt")
                it.demandValue("type", "NySøknad")
                it.requireKey("søknadsData.brukerBehandlingId")
                it.requireKey("fødselsnummer")
                it.requireKey("aktørId")
            }
        }.register(this)
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) {
        log.info { "Mottok søknad med id ${packet["søknadsId"].asText()}" }
        val identer = Identer.Builder()
            .folkeregisterIdent(packet["fødselsnummer"].asText())
            .aktørId(packet["aktørId"].asText())
            .build()
        val søknadsId = packet["søknadsData.brukerBehandlingId"].asText()
        val faktagrupperType = Versjon.UserInterfaceType.Web
    }

    override fun onError(problems: MessageProblems, context: MessageContext) {
        log.error { problems.toString() }
        sikkerLogg.error { problems.toExtendedReport() }
    }
}
