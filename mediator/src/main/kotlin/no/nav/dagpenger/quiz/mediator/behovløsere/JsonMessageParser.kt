package no.nav.dagpenger.quiz.mediator.behovløsere

import com.github.navikt.tbd_libs.rapids_and_rivers.JsonMessage
import java.util.UUID

internal fun JsonMessage.søknadUUID() = this["søknad_uuid"].asText().let { UUID.fromString(it) }
