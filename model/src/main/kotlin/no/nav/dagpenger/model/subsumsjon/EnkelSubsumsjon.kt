package no.nav.dagpenger.model.subsumsjon

import no.nav.dagpenger.model.faktagrupper.Faktagrupper
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.faktum.Faktum
import no.nav.dagpenger.model.faktum.Faktum.Companion.deepCopy
import no.nav.dagpenger.model.faktum.Faktum.FaktumTilstand.Ukjent
import no.nav.dagpenger.model.faktum.GrunnleggendeFaktum
import no.nav.dagpenger.model.faktum.erBesvart
import no.nav.dagpenger.model.regel.Regel
import no.nav.dagpenger.model.visitor.SubsumsjonVisitor

open class EnkelSubsumsjon protected constructor(
    protected val regel: Regel,
    protected val subsumsjonFakta: Set<Faktum<*>>,
    gyldigSubsumsjon: Subsumsjon,
    ugyldigSubsumsjon: Subsumsjon
) : Subsumsjon(regel.toString(), gyldigSubsumsjon, ugyldigSubsumsjon) {

    internal constructor(regel: Regel, vararg fakta: Faktum<*>) :
        this(regel, fakta.toSet(), TomSubsumsjon, TomSubsumsjon)

    override fun accept(visitor: SubsumsjonVisitor) {
        resultat().also {
            visitor.preVisit(this, regel, subsumsjonFakta, it)
            subsumsjonFakta.forEach { it.accept(visitor) }
            super.accept(visitor)
            visitor.postVisit(this, regel, subsumsjonFakta, it)
        }
    }

    override fun deepCopy(faktagrupper: Faktagrupper) = deepCopy(
        regel.deepCopy(faktagrupper),
        subsumsjonFakta.deepCopy(faktagrupper),
        gyldigSubsumsjon.deepCopy(faktagrupper),
        ugyldigSubsumsjon.deepCopy(faktagrupper)
    )
    override fun bygg(søknad: Søknad) = deepCopy(
        regel.bygg(søknad),
        this.subsumsjonFakta.map { søknad.id(it.faktumId) }.toSet(),
        gyldigSubsumsjon.bygg(søknad),
        ugyldigSubsumsjon.bygg(søknad)
    )

    override fun deepCopy() = deepCopy(
        regel,
        subsumsjonFakta,
        gyldigSubsumsjon.deepCopy(),
        ugyldigSubsumsjon.deepCopy()
    )

    override fun deepCopy(indeks: Int, søknad: Søknad) = deepCopy(
        regel.deepCopy(indeks, søknad),
        subsumsjonFakta.deepCopy(indeks, søknad),
        gyldigSubsumsjon.deepCopy(indeks, søknad),
        ugyldigSubsumsjon.deepCopy(indeks, søknad)
    )

    private fun deepCopy(
        regel: Regel,
        fakta: Set<Faktum<*>>,
        gyldigSubsumsjon: Subsumsjon,
        ugyldigSubsumsjon: Subsumsjon
    ) = EnkelSubsumsjon(regel, fakta, gyldigSubsumsjon, ugyldigSubsumsjon)

    override fun nesteFakta() = ukjenteFakta().takeIf { it.isNotEmpty() } ?: nesteSubsumsjon().nesteFakta()

    internal open fun ukjenteFakta(): Set<GrunnleggendeFaktum<*>> = mutableSetOf<GrunnleggendeFaktum<*>>().also {
        subsumsjonFakta.forEach { faktum -> faktum.leggTilHvis(Ukjent, it) }
    }

    private fun nesteSubsumsjon() = if (lokaltResultat() == true) gyldigSubsumsjon else ugyldigSubsumsjon

    override fun lokaltResultat() = if (subsumsjonFakta.erBesvart()) regel.resultat() else null

    override fun toString() = regel.toString()

    override operator fun get(indeks: Int) = throw IllegalArgumentException()

    override fun iterator(): Iterator<Subsumsjon> {
        return object : Iterator<Subsumsjon> {
            override fun hasNext() = false
            override fun next() = throw NoSuchElementException()
        }
    }
}
