package no.nav.dagpenger.model.søknad

import no.nav.dagpenger.model.fakta.GrunnleggendeFaktum

class Seksjon(vararg fakta: GrunnleggendeFaktum<*>) {
    private val fakta = fakta.toSet()
    internal operator fun contains(nesteFakta: Set<GrunnleggendeFaktum<*>>): Boolean {
        return nesteFakta.any { it in fakta }
    }
}
