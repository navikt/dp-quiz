package no.nav.dagpenger.quiz.mediator.meldinger

import com.github.navikt.tbd_libs.rapids_and_rivers.JsonMessage
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageContext
import io.github.oshai.kotlinlogging.KotlinLogging
import jsonStringToMap
import no.nav.dagpenger.model.seksjon.Prosess

private val log = KotlinLogging.logger {}
private val sikkerlogg = KotlinLogging.logger("tjenestekall")

fun Prosess.sendNesteSeksjon(context: MessageContext) {
    nesteSeksjoner()
        .onEach { seksjon ->
            val message =
                seksjon.somSpørsmål().let {
                    JsonMessage.newMessage(jsonStringToMap(it))
                }

            context.publish(message.toJson())
            sikkerlogg.info { "Send ut seksjon: ${message.toJson()}" }
            log.info { "Send seksjon ${seksjon.navn} for søknad ${fakta.uuid}" }
        }
}
