package no.nav.dagpenger.quiz.mediator.meldinger

import com.github.navikt.tbd_libs.rapids_and_rivers.JsonMessage
import com.github.navikt.tbd_libs.rapids_and_rivers.River
import com.github.navikt.tbd_libs.rapids_and_rivers.withMDC
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageContext
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageMetadata
import com.github.navikt.tbd_libs.rapids_and_rivers_api.RapidsConnection
import io.micrometer.core.instrument.MeterRegistry
import mu.KotlinLogging
import no.nav.dagpenger.quiz.mediator.db.ProsessRepository
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
        River(rapidsConnection)
            .apply {
                precondition { it.requireValue("@event_name", "søknad_slettet") }
                validate { it.requireKey("søknad_uuid") }
            }.register(this)
    }

    override fun onPacket(
        packet: JsonMessage,
        context: MessageContext,
        metadata: MessageMetadata,
        meterRegistry: MeterRegistry,
    ) {
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
