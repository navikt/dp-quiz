package no.nav.dagpenger.quiz.mediator.soknad

import no.nav.dagpenger.model.faktum.Prosessnavn

enum class Prosess(override val id: String) : Prosessnavn {
    Aldersvurdering("Aldersvurdering"),
    Dagpenger("Dagpenger"),
    AvslagPåMinsteinntekt("AvslagPåMinsteinntekt"),
    Innsending("Innsending"),
}
