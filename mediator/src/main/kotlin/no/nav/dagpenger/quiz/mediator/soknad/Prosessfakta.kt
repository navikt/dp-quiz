package no.nav.dagpenger.quiz.mediator.soknad

import no.nav.dagpenger.model.faktum.Faktatype

enum class Prosessfakta(
    override val id: String,
) : Faktatype {
    Dagpenger("Dagpenger"),
    Innsending("Innsending"),
}
