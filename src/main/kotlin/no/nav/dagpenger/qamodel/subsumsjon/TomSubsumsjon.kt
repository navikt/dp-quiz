package no.nav.dagpenger.qamodel.subsumsjon

import no.nav.dagpenger.qamodel.fakta.Faktum
import java.lang.IndexOutOfBoundsException

internal object TomSubsumsjon : Subsumsjon("Tom subsumsjon") {
    override fun konkluder() = true

    override fun fakta(): Set<Faktum<*>> = emptySet()

    override fun nesteFakta(): Set<Faktum<*>> = emptySet()

    override fun subsumsjoner(vararg fakta: Faktum<*>) = emptyList<EnkelSubsumsjon>()

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
