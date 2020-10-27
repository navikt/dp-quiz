package meldinger.model

import no.nav.dagpenger.model.marshalling.FaktumJsonBuilder
import no.nav.dagpenger.model.marshalling.SeksjonJsonBuilder
import no.nav.dagpenger.model.søknad.Seksjon
import no.nav.helse.rapids_rivers.JsonMessage
import java.time.LocalDateTime
import java.util.UUID

internal class BehovMelding(private val fødselsnummer: String, private val seksjon: Seksjon) {
    private val eventName = "behov"
    private val id = UUID.randomUUID()
    private val opprettet = LocalDateTime.now()
    private val behov = seksjon.map { it.navn }
    private val fakta = seksjon.map { it }

    fun toJson() = JsonMessage.newMessage(
        mutableMapOf(
            "@event_name" to eventName,
            "@opprettet" to opprettet,
            "@id" to id,
            "@behov" to behov,
            "fødselsnummer" to fødselsnummer,
            "fakta" to fakta
        )
    ).toJson()
}

