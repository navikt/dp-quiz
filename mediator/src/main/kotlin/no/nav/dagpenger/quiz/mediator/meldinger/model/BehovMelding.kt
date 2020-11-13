package no.nav.dagpenger.quiz.mediator.meldinger.model

import no.nav.dagpenger.model.faktagrupper.Seksjon
import no.nav.dagpenger.model.marshalling.SeksjonJsonBuilder
import no.nav.helse.rapids_rivers.JsonMessage
import java.time.LocalDateTime
import java.util.UUID

internal class BehovMelding(private val fnr: String, private val seksjon: Seksjon, private val søknadUuid: UUID) {
    private val eventName = "behov"
    private val id = UUID.randomUUID()
    private val opprettet = LocalDateTime.now()
    private val fakta = SeksjonJsonBuilder(seksjon).resultat()["fakta"]

    fun toJson() = JsonMessage.newMessage(
        mutableMapOf(
            "@event_name" to eventName,
            "@opprettet" to opprettet,
            "@id" to id,
            "fnr" to fnr,
            "fakta" to fakta,
            "søknadUuid" to søknadUuid,
            "seksjonsnavn" to seksjon.navn,
            // "versjonId" to versjonId,
        )
    ).toJson()
}
