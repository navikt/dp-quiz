package no.nav.dagpenger.quiz.mediator.soknad

import no.nav.dagpenger.model.faktum.Prosessnavn

enum class Prosess(override val id: String) : Prosessnavn {
    Dagpenger("Dagpenger"),
    AvslagPåMinsteinntekt("AvslagPåMinsteinntekt"),
}
