package no.nav.dagpenger.quiz.mediator

import mu.KotlinLogging

private val sikkerlogg = KotlinLogging.logger("tjenestekall")

fun main() {
    sikkerlogg.info { Configuration.config }
    ApplicationBuilder().start()
}
