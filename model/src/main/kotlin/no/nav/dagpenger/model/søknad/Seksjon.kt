package no.nav.dagpenger.model.søknad

import no.nav.dagpenger.model.fakta.Faktum

class Seksjon(vararg fakta: Faktum<*>) {
    private val fakta = fakta.toSet()
    internal operator fun contains(nesteFakta: Set<Faktum<*>>): Boolean {
        return nesteFakta.any { it in fakta }
    }
}
