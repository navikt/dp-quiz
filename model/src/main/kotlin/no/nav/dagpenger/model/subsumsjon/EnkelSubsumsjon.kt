package no.nav.dagpenger.model.subsumsjon

import no.nav.dagpenger.model.fakta.Faktum
import no.nav.dagpenger.model.fakta.Faktum.FaktumTilstand.Ukjent
import no.nav.dagpenger.model.fakta.GrunnleggendeFaktum
import no.nav.dagpenger.model.fakta.erBesvart
import no.nav.dagpenger.model.regel.Regel
import no.nav.dagpenger.model.visitor.SubsumsjonVisitor

open class EnkelSubsumsjon internal constructor(
    private val regel: Regel,
    vararg fakta: Faktum<*>
) : Subsumsjon(regel.toString()) {
    private val fakta = fakta.toSet()

    override fun accept(visitor: SubsumsjonVisitor) {
        visitor.preVisit(this, regel, fakta)
        fakta.forEach { it.accept(visitor) }
        super.accept(visitor)
        visitor.postVisit(this, regel, fakta)
    }

    override fun fakta() = fakta

    override fun nesteFakta() = ukjenteFakta().takeIf { it.isNotEmpty() } ?: nesteSubsumsjon().nesteFakta()

    open internal fun ukjenteFakta(): Set<GrunnleggendeFaktum<*>> = mutableSetOf<GrunnleggendeFaktum<*>>().also {
        fakta.forEach { faktum -> faktum.leggTilHvis(Ukjent, it) }
    }

    private fun nesteSubsumsjon() = if (lokaltResultat() == true) gyldigSubsumsjon else ugyldigSubsumsjon

    override fun _sti(subsumsjon: Subsumsjon) = if (this == subsumsjon) listOf(this) else emptyList()

    override fun lokaltResultat() = if (fakta.erBesvart()) regel.resultat() else null

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
