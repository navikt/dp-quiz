package no.nav.dagpenger.quiz.mediator.meldinger

import mu.KotlinLogging
import no.nav.dagpenger.model.seksjon.Søknadprosess
import no.nav.helse.rapids_rivers.MessageContext

private val log = KotlinLogging.logger {}
private val sikkerlogg = KotlinLogging.logger("tjenestekall")

fun Søknadprosess.sendNesteSeksjon(context: MessageContext) {
    nesteSeksjoner()
        .onEach { seksjon ->
            val json = seksjon.somSpørsmål()
            context.publish(json)
            sikkerlogg.info { "Send ut seksjon: $json" }
            log.info { "Send seksjon ${seksjon.navn} for søknad ${søknad.uuid}" }
        }
}
