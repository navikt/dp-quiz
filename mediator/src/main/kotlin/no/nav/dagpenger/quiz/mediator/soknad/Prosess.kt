package no.nav.dagpenger.quiz.mediator.soknad

import no.nav.dagpenger.model.faktum.Faktatype

enum class Prosess(override val id: String) : Faktatype {
    Paragraf_4_23_alder("Paragraf_4_23_alder"),
    Dagpenger("Dagpenger"),
    AvslagPåMinsteinntekt("AvslagPåMinsteinntekt"),
    Innsending("Innsending"),
}
