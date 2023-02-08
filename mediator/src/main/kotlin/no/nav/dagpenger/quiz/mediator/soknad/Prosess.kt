package no.nav.dagpenger.quiz.mediator.soknad

import no.nav.dagpenger.model.faktum.Prosessnavn

enum class Prosess(override val id: String) : Prosessnavn {
    Paragraf_4_23_alder("Paragraf_4_23_alder"),
    Dagpenger("Dagpenger"),
    AvslagPåMinsteinntekt("AvslagPåMinsteinntekt"),
    Innsending("Innsending"),
}
