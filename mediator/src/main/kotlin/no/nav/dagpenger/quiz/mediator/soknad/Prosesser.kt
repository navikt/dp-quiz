package no.nav.dagpenger.quiz.mediator.soknad

import no.nav.dagpenger.model.faktum.Faktatype
import no.nav.dagpenger.model.seksjon.Prosesstype

enum class Prosesser(override val faktatype: Faktatype) : Prosesstype {
    Søknad(Prosessfakta.Dagpenger),
    Dokumentkrav(Prosessfakta.Dagpenger),
    AvslagPåAlder(Prosessfakta.Dagpenger),
    Paragraf_4_23_alder(Prosessfakta.Paragraf_4_23_alder),
    AvslagPåMinsteinntekt(Prosessfakta.AvslagPåMinsteinntekt),
    Innsending(Prosessfakta.Innsending),
}
