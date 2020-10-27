package meldinger.model

import no.nav.helse.rapids_rivers.JsonMessage
import java.time.LocalDateTime
import java.util.UUID

internal class BehovMelding(private val fødselsnummer: String, vararg behov: String) {
    private val eventName = "behov"
    private val id = UUID.randomUUID()
    private val opprettet = LocalDateTime.now()
    private val behov = behov.toList()

    fun toJson() = JsonMessage.newMessage(
        mutableMapOf(
            "@event_name" to eventName,
            "@opprettet" to opprettet,
            "@id" to id,
            "@behov" to behov,
            "fødselsnummer" to fødselsnummer
        )
    ).toJson()
}

