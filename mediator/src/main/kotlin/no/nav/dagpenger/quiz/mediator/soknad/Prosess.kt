package no.nav.dagpenger.quiz.mediator.soknad

import no.nav.dagpenger.model.faktum.HenvendelsesType

enum class Prosess(override val id: String) : HenvendelsesType {
    Paragraf_4_23_alder("Paragraf_4_23_alder"),
    Dagpenger("Dagpenger"),
    AvslagPåMinsteinntekt("AvslagPåMinsteinntekt"),
    Innsending("Innsending"),
}
