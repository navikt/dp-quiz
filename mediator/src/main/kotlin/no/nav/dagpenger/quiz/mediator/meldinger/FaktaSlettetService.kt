package no.nav.dagpenger.quiz.mediator.meldinger

import mu.KotlinLogging
import no.nav.dagpenger.quiz.mediator.db.ProsessRepository
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River
import no.nav.helse.rapids_rivers.withMDC
import java.util.UUID

internal class FaktaSlettetService(
    rapidsConnection: RapidsConnection,
    private val prosessRepository: ProsessRepository,
) : River.PacketListener {

    companion object {
        private val logger = KotlinLogging.logger {}
        private val sikkerlogg = KotlinLogging.logger("tjenestekall")
    }

    init {
        River(rapidsConnection).apply {
            validate { it.demandValue("@event_name", "søknad_slettet") }
            validate { it.requireKey("søknad_uuid") }
        }.register(this)
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) {
        val uuid = UUID.fromString(packet["søknad_uuid"].asText())
        withMDC("søknad_uuid" to uuid.toString()) {
            sikkerlogg.info { "Mottok sletteevent: ${packet.toJson()}" }
            try {
                logger.info { "Forsøker å slette søknad: $uuid" }
                prosessRepository.slett(uuid)
            } catch (e: Exception) {
                sikkerlogg.error(e) { "Sletting av søknad med uuid: $uuid feilet" }
            }
        }
    }
}
