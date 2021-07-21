package no.nav.dagpenger.quiz.mediator.meldinger

import mu.KotlinLogging
import no.nav.dagpenger.quiz.mediator.db.ResultatPersistence
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.MessageProblems
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River
import java.util.UUID

class ManuellBehandlingSink(
    rapidsConnection: RapidsConnection,
    private val resultatPersistence: ResultatPersistence
) : River.PacketListener {

    companion object {
        private val log = KotlinLogging.logger {}
        private val sikkerlogg = KotlinLogging.logger("tjenestekall")
    }

    init {
        River(rapidsConnection).apply {
            validate {
                it.demandValue("@event_name", "manuell_behandling")
                it.requireKey("søknad_uuid", "seksjon_navn")
            }
        }.register(this)
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) {
        log.info { "Mottok melding om manuell behandling" }
        val søknadUuid = UUID.fromString(packet["søknad_uuid"].asText())
        val seksjonNavn = packet["seksjon_navn"].asText()

        resultatPersistence.lagreManuellBehandling(søknadUuid, seksjonNavn)
    }

    override fun onError(problems: MessageProblems, context: MessageContext) {
        log.error { problems.toString() }
        sikkerlogg.error { problems.toExtendedReport() }
    }
}
