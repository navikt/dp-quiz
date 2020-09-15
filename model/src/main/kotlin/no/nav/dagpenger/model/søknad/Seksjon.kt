package no.nav.dagpenger.model.søknad

import no.nav.dagpenger.model.unit.fakta.Faktum
import no.nav.dagpenger.model.unit.fakta.GrunnleggendeFaktum

class Seksjon(vararg fakta: Faktum<*>) : Collection<Faktum<*>> by fakta.toFaktaSet() {
    private val fakta = fakta.toFaktaSet()

    internal operator fun contains(nesteFakta: Set<GrunnleggendeFaktum<*>>): Boolean {
        return nesteFakta.any { it in fakta }
    }
}

private fun <T : Faktum<*>> Array<T>.toFaktaSet(): Set<GrunnleggendeFaktum<*>> =
    this.flatMap { it.grunnleggendeFakta() }.toSet()
