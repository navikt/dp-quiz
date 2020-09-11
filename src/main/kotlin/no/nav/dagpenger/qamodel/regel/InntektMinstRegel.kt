package no.nav.dagpenger.qamodel.regel

import no.nav.dagpenger.qamodel.fakta.Faktum
import no.nav.dagpenger.qamodel.port.Inntekt

internal class InntektMinstRegel(
    private val faktisk: Faktum<Inntekt>,
    private val terskel: Faktum<Inntekt>
) : Regel {
    override fun konkluder() = faktisk.svar() >= terskel.svar()
    override fun toString() = "Sjekk at '${faktisk.navn}' er minst '${terskel.navn}'"
}
