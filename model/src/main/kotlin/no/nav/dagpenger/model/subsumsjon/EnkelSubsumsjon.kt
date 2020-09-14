package no.nav.dagpenger.model.subsumsjon

import no.nav.dagpenger.model.fakta.Faktum
import no.nav.dagpenger.model.fakta.erBesvart
import no.nav.dagpenger.model.regel.Regel
import no.nav.dagpenger.model.visitor.SubsumsjonVisitor

class EnkelSubsumsjon internal constructor(
        private val regel: Regel,
        vararg fakta: Faktum<*>
) : Subsumsjon(regel.toString()) {
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

    override fun nesteFakta() = ukjenteFakta().takeIf { it.isNotEmpty() } ?: nesteSubsumsjon().nesteFakta()

    private fun ukjenteFakta() = mutableSetOf<Faktum<*>>().also {
        fakta.forEach { faktum -> faktum.leggTilHvis(Faktum.FaktumTilstand.Ukjent, it) } }

    private fun nesteSubsumsjon() = if (konkluder()) gyldigSubsumsjon else ugyldigSubsumsjon

    override fun _sti(subsumsjon: Subsumsjon) = if (this == subsumsjon) listOf(this) else emptyList()

    override fun _resultat() = if (fakta.erBesvart()) konkluder() else null

    override fun enkelSubsumsjoner(vararg fakta: Faktum<*>): List<EnkelSubsumsjon> =
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
