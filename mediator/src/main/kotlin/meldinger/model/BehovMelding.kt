package meldinger.model

import no.nav.dagpenger.model.faktagrupper.Seksjon
import no.nav.dagpenger.model.marshalling.SeksjonJsonBuilder
import no.nav.helse.rapids_rivers.JsonMessage
import java.time.LocalDateTime
import java.util.UUID

internal class BehovMelding(private val fødselsnummer: String, private val seksjon: Seksjon, private val søknadUuid: UUID) {
    private val eventName = "behov"
    private val id = UUID.randomUUID()
    private val opprettet = LocalDateTime.now()
    private val behov = seksjon.map { it.navn }
    private val fakta = SeksjonJsonBuilder(seksjon).resultat()["fakta"]

    fun toJson() = JsonMessage.newMessage(
        mutableMapOf(
            "@event_name" to eventName,
            "@opprettet" to opprettet,
            "@id" to id,
            "@behov" to behov,
            "fødselsnummer" to fødselsnummer,
            "fakta" to fakta,
            "søknadUuid" to søknadUuid,
            "seksjonNavn" to seksjon.navn,
            // "versjonId" to versjonId,

        )
    ).toJson()
}
