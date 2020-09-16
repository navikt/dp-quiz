package no.nav.dagpenger.model.subsumsjon

import no.nav.dagpenger.model.fakta.Faktum
import no.nav.dagpenger.model.fakta.GrunnleggendeFaktum
import no.nav.dagpenger.model.visitor.SubsumsjonVisitor

internal object TomSubsumsjon : Subsumsjon("Tom subsumsjon") {
    override fun konkluder() = true

    override fun fakta(): Set<GrunnleggendeFaktum<*>> = emptySet()

    override fun accept(visitor: SubsumsjonVisitor) {}

    override fun nesteFakta(): Set<GrunnleggendeFaktum<*>> = emptySet()

    override fun enkelSubsumsjoner(vararg fakta: Faktum<*>) = emptyList<EnkelSubsumsjon>()

    override fun _sti(subsumsjon: Subsumsjon) = if (this == subsumsjon) listOf(this) else emptyList()

    override fun _resultat() = throw IllegalStateException()

    override operator fun get(indeks: Int) = throw IllegalArgumentException()

    override fun iterator(): Iterator<Subsumsjon> {
        return object : Iterator<Subsumsjon> {
            override fun hasNext() = false
            override fun next() = throw NoSuchElementException()
        }
    }
}
