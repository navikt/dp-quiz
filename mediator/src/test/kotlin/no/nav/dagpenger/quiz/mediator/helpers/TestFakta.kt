package no.nav.dagpenger.quiz.mediator.helpers

import no.nav.dagpenger.model.faktum.Fakta
import no.nav.dagpenger.model.marshalling.FaktumNavBehov
import no.nav.dagpenger.model.seksjon.Henvendelser

// TODO: Denne bør kun brukes når vi MÅ ha global state
fun Fakta.registrer(
    faktumNavBehov: FaktumNavBehov? = null,
    prosess: Henvendelser.FaktaBygger.(fakta: Fakta) -> Unit = {},
) = this.also { prototypeFakta ->
    Henvendelser.FaktaBygger(prototypeFakta, faktumNavBehov).apply { prosess(prototypeFakta) }.registrer()
}
