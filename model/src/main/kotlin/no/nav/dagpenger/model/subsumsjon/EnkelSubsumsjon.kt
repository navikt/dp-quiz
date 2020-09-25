package no.nav.dagpenger.model.subsumsjon

import no.nav.dagpenger.model.fakta.Faktum
import no.nav.dagpenger.model.fakta.Faktum.FaktumTilstand.Ukjent
import no.nav.dagpenger.model.fakta.FaktumNavn
import no.nav.dagpenger.model.fakta.GrunnleggendeFaktum
import no.nav.dagpenger.model.fakta.deepCopy
import no.nav.dagpenger.model.fakta.erBesvart
import no.nav.dagpenger.model.regel.Regel
import no.nav.dagpenger.model.visitor.SubsumsjonVisitor

open class EnkelSubsumsjon protected constructor(
    private val regel: Regel,
    private val fakta: Set<Faktum<*>>,
    gyldigSubsumsjon: Subsumsjon,
    ugyldigSubsumsjon: Subsumsjon
) : Subsumsjon(regel.toString(), gyldigSubsumsjon, ugyldigSubsumsjon) {

    internal constructor(regel: Regel, vararg fakta: Faktum<*>) :
        this(regel, fakta.toSet(), TomSubsumsjon, TomSubsumsjon)

    override fun accept(visitor: SubsumsjonVisitor) {
        visitor.preVisit(this, regel, fakta)
        fakta.forEach { it.accept(visitor) }
        super.accept(visitor)
        visitor.postVisit(this, regel, fakta)
    }

    override fun deepCopy(faktaMap: Map<FaktumNavn, Faktum<*>>) = EnkelSubsumsjon(
        regel.deepCopy(faktaMap),
        fakta.deepCopy(faktaMap),
        gyldigSubsumsjon.deepCopy(faktaMap),
        ugyldigSubsumsjon.deepCopy(faktaMap)
    )

    override fun fakta() = fakta

    override fun nesteFakta() = ukjenteFakta().takeIf { it.isNotEmpty() } ?: nesteSubsumsjon().nesteFakta()

    internal open fun ukjenteFakta(): Set<GrunnleggendeFaktum<*>> = mutableSetOf<GrunnleggendeFaktum<*>>().also {
        fakta.forEach { faktum -> faktum.leggTilHvis(Ukjent, it) }
    }

    private fun nesteSubsumsjon() = if (lokaltResultat() == true) gyldigSubsumsjon else ugyldigSubsumsjon

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
