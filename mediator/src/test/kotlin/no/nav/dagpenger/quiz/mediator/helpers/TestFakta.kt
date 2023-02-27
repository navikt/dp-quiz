package no.nav.dagpenger.quiz.mediator.helpers

import no.nav.dagpenger.model.faktum.Fakta
import no.nav.dagpenger.model.marshalling.FaktumNavBehov
import no.nav.dagpenger.model.seksjon.FaktaVersjonDingseboms

fun Fakta.registrer(faktumNavBehov: FaktumNavBehov? = null) = this.also { prototypeFakta ->
    FaktaVersjonDingseboms.Bygger(prototypeFakta, faktumNavBehov).registrer()
}
