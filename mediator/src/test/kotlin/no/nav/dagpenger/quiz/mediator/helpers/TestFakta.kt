package no.nav.dagpenger.quiz.mediator.helpers

import no.nav.dagpenger.model.faktum.Fakta
import no.nav.dagpenger.model.seksjon.FaktaVersjonDingseboms

fun Fakta.registrer() = this.also { prototypeFakta ->
    FaktaVersjonDingseboms.Bygger(prototypeFakta).registrer()
}
