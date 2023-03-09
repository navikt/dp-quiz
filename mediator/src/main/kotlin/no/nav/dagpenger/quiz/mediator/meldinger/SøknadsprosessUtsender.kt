package no.nav.dagpenger.quiz.mediator.meldinger

import mu.KotlinLogging
import no.nav.dagpenger.model.seksjon.Prosess
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.MessageProblems

private val log = KotlinLogging.logger {}
private val sikkerlogg = KotlinLogging.logger("tjenestekall")

fun Prosess.sendNesteSeksjon(context: MessageContext) {
    nesteSeksjoner()
        .onEach { seksjon ->
            val message = seksjon.somSpørsmål().let { JsonMessage(it, MessageProblems(it)) }
            context.publish(message.toJson())
            sikkerlogg.info { "Send ut seksjon: ${message.toJson()}" }
            log.info { "Send seksjon ${seksjon.navn} for søknad ${fakta.uuid}" }
        }
}
