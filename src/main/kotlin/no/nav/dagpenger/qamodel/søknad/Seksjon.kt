package no.nav.dagpenger.qamodel.søknad

import no.nav.dagpenger.qamodel.fakta.Faktum

class Seksjon(vararg fakta: Faktum<*>) {
    private val fakta = fakta.toSet()
    internal operator fun contains(nesteFakta: Set<Faktum<*>>): Boolean {
        return nesteFakta.any { it in fakta }
    }
}
