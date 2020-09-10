package no.nav.dagpenger.qamodel.regel

import no.nav.dagpenger.qamodel.fakta.Faktum
import no.nav.dagpenger.qamodel.port.Inntekt

internal object InntektMinstRegel : Regel {
    override fun konkluder(fakta: Set<Faktum<*>>): Boolean {
        return konkluder(fakta.first().svar() as Inntekt, fakta.last().svar() as Inntekt)
    }

    private fun konkluder(faktiskInntekt: Inntekt, terskel: Inntekt) =
        faktiskInntekt >= terskel
}
