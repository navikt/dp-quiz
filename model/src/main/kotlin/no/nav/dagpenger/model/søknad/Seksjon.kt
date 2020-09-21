package no.nav.dagpenger.model.søknad

import no.nav.dagpenger.model.fakta.Faktum
import no.nav.dagpenger.model.fakta.GrunnleggendeFaktum
import no.nav.dagpenger.model.visitor.SøknadVisitor

class Seksjon(vararg fakta: Faktum<*>) : Collection<Faktum<*>> by fakta.toFaktaSet() {
    private val fakta = fakta.toFaktaSet()

    internal operator fun contains(nesteFakta: Set<GrunnleggendeFaktum<*>>): Boolean {
        return nesteFakta.any { it in fakta }
    }

    fun accept(visitor: SøknadVisitor) {
        visitor.preVisit(this, fakta)
        fakta.forEach { it.accept(visitor) }
        visitor.postVisit(this)
    }
}

private fun <T : Faktum<*>> Array<T>.toFaktaSet(): Set<GrunnleggendeFaktum<*>> =
    this.flatMap { it.grunnleggendeFakta() }.toSet()
