package no.nav.dagpenger.qamodel.subsumsjon

import no.nav.dagpenger.qamodel.fakta.Faktum
import no.nav.dagpenger.qamodel.fakta.erBesvart
import no.nav.dagpenger.qamodel.regel.Regel
import no.nav.dagpenger.qamodel.visitor.SubsumsjonVisitor

class EnkelSubsumsjon internal constructor(
    private val regel: Regel,
    vararg fakta: Faktum<*>
) : Subsumsjon("Enkel subsumsjon") {
    private val fakta = fakta.toSet()

    override fun konkluder() = regel.konkluder()

    override fun accept(visitor: SubsumsjonVisitor) {
        visitor.preVisit(this, regel)
        fakta.forEach { it.accept(visitor) }
        acceptGyldig(visitor)
        acceptUgyldig(visitor)
        visitor.postVisit(this, regel)
    }

    override fun fakta(): Set<Faktum<*>> = fakta

    override fun nesteFakta(): Set<Faktum<*>> {
        return mutableSetOf<Faktum<*>>().also {
            fakta.forEach { faktum -> faktum.leggTilHvis(Faktum.FaktumTilstand.Ukjent, it) }
            if (it.isNotEmpty()) return it
            (if (konkluder()) gyldigSubsumsjon else ugyldigSubsumsjon).nesteFakta()
        }
    }

    override fun _sti(subsumsjon: Subsumsjon) = if (this == subsumsjon) listOf(this) else emptyList()

    override fun _resultat() = if(fakta.erBesvart()) konkluder() else null

    override fun subsumsjoner(vararg fakta: Faktum<*>): List<EnkelSubsumsjon> =
        if (fakta.any { it in this.fakta }) listOf(this) else emptyList()

    override fun toString() = regel.toString()

    override operator fun get(indeks: Int) = throw IllegalArgumentException()

    override fun iterator(): Iterator<Subsumsjon> {
        return object : Iterator<Subsumsjon> {
            override fun hasNext() = false
            override fun next() = throw NoSuchElementException()
        }
    }
}
