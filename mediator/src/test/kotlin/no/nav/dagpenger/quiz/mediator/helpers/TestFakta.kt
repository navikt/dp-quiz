package no.nav.dagpenger.quiz.mediator.helpers

import no.nav.dagpenger.model.faktum.Fakta
import no.nav.dagpenger.model.marshalling.FaktumNavBehov
import no.nav.dagpenger.model.seksjon.FaktaVersjonDingseboms

fun Fakta.registrer(
    faktumNavBehov: FaktumNavBehov? = null,
    prosess: FaktaVersjonDingseboms.Bygger.(fakta: Fakta) -> Unit = {},
) = this.also { prototypeFakta ->
    FaktaVersjonDingseboms.Bygger(prototypeFakta, faktumNavBehov).apply { prosess(prototypeFakta) }.registrer()
}
