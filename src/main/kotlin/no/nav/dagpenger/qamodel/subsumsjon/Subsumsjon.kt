package no.nav.dagpenger.qamodel.subsumsjon

import no.nav.dagpenger.qamodel.fakta.Faktum
import no.nav.dagpenger.qamodel.regel.Regel

class Subsumsjon(private val regel: Regel, vararg fakta: Faktum<*>) {
    private val fakta = fakta.toList()
    fun konkluder() = regel.konkluder(fakta)
}