package no.nav.dagpenger.quiz.mediator.meldinger

import com.github.navikt.tbd_libs.rapids_and_rivers.JsonMessage
import com.github.navikt.tbd_libs.rapids_and_rivers.River
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageContext
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageMetadata
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageProblems
import com.github.navikt.tbd_libs.rapids_and_rivers_api.RapidsConnection
import io.micrometer.core.instrument.MeterRegistry
import mu.KotlinLogging
import no.nav.dagpenger.quiz.mediator.db.ResultatPersistence
import java.util.UUID

internal class ManuellBehandlingSink(
    rapidsConnection: RapidsConnection,
    private val resultatPersistence: ResultatPersistence,
) : River.PacketListener {
    companion object {
        private val log = KotlinLogging.logger {}
        private val sikkerlogg = KotlinLogging.logger("tjenestekall")
    }

    init {
        River(rapidsConnection).apply {
            precondition { it.requireValue("@event_name", "manuell_behandling") }
            validate { it.requireKey("søknad_uuid", "seksjon_navn") }
        }.register(this)
    }

    override fun onPacket(
        packet: JsonMessage,
        context: MessageContext,
        metadata: MessageMetadata,
        meterRegistry: MeterRegistry,
    ) {
        val søknadUuid = UUID.fromString(packet["søknad_uuid"].asText())
        log.info { "Mottok melding om manuell behandling for søknad $søknadUuid" }
        val seksjonNavn = packet["seksjon_navn"].asText()

        resultatPersistence.lagreManuellBehandling(søknadUuid, seksjonNavn)
    }

    override fun onError(
        problems: MessageProblems,
        context: MessageContext,
        metadata: MessageMetadata,
    ) {
        log.error { problems.toString() }
        sikkerlogg.error { problems.toExtendedReport() }
    }
}
