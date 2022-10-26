package no.nav.dagpenger.quiz.mediator.behovløsere

import no.nav.helse.rapids_rivers.JsonMessage
import java.util.UUID

internal fun JsonMessage.søknadUUID() = this["søknad_uuid"].asText().let { UUID.fromString(it) }
