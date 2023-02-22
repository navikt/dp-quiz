package no.nav.dagpenger.quiz.mediator.soknad

import no.nav.dagpenger.model.faktum.Faktatype
import no.nav.dagpenger.model.seksjon.Prosesstype

enum class Prosesser(override val navn: String, override val faktatype: Faktatype) : Prosesstype {
    Søknad("Søknad", Prosessfakta.Dagpenger),
    Dokumentkrav("Dokumentkrav", Prosessfakta.Dagpenger),
    AvslagPåAlder("AvslagPåAlder", Prosessfakta.Dagpenger),
    Paragraf_4_23_alder("Paragraf_4_23_alder", Prosessfakta.Paragraf_4_23_alder),
    AvslagPåMinsteinntekt("AvslagPåMinsteinntekt", Prosessfakta.AvslagPåMinsteinntekt),
    Innsending("Innsending", Prosessfakta.Innsending),
}
