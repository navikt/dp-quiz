package no.nav.dagpenger.model.subsumsjon

import no.nav.dagpenger.model.fakta.Faktum
import no.nav.dagpenger.model.fakta.Faktum.FaktumTilstand.Ukjent
import no.nav.dagpenger.model.fakta.GrunnleggendeFaktum
import no.nav.dagpenger.model.fakta.deepCopy
import no.nav.dagpenger.model.fakta.erBesvart
import no.nav.dagpenger.model.regel.Regel
import no.nav.dagpenger.model.søknad.Søknad
import no.nav.dagpenger.model.visitor.SubsumsjonVisitor

open class EnkelSubsumsjon protected constructor(
    protected val regel: Regel,
    protected val fakta: Set<Faktum<*>>,
    gyldigSubsumsjon: Subsumsjon,
    ugyldigSubsumsjon: Subsumsjon
) : Subsumsjon(regel.toString(), gyldigSubsumsjon, ugyldigSubsumsjon) {

    internal constructor(regel: Regel, vararg fakta: Faktum<*>) :
        this(regel, fakta.toSet(), TomSubsumsjon, TomSubsumsjon)

    override fun accept(visitor: SubsumsjonVisitor) {
        resultat().also {
            visitor.preVisit(this, regel, fakta, it)
            fakta.forEach { it.accept(visitor) }
            super.accept(visitor)
            visitor.postVisit(this, regel, fakta, it)
        }
    }

    override fun deepCopy(søknad: Søknad) = EnkelSubsumsjon(
        regel.deepCopy(søknad),
        fakta.deepCopy(søknad),
        gyldigSubsumsjon.deepCopy(søknad),
        ugyldigSubsumsjon.deepCopy(søknad)
    ).also {
        it.søknad = søknad
    }

    override fun deepCopy() = EnkelSubsumsjon(
        regel,
        fakta,
        gyldigSubsumsjon.deepCopy(),
        ugyldigSubsumsjon.deepCopy()
    )

    override fun deepCopy(indeks: Int) = EnkelSubsumsjon(
        regel.deepCopy(indeks, søknad),
        fakta.deepCopy(indeks, søknad),
        gyldigSubsumsjon.deepCopy(indeks),
        ugyldigSubsumsjon.deepCopy(indeks)
    )

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
