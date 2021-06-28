package no.nav.dagpenger.model.subsumsjon

import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.seksjon.Søknadprosess
import no.nav.dagpenger.model.visitor.SubsumsjonVisitor

// Evalueres til resultatet av det underliggende subsumsjonstreet
class DeltreSubsumsjon private constructor(
    navn: String,
    private val child: Subsumsjon,
    gyldigSubsumsjon: Subsumsjon,
    ugyldigSubsumsjon: Subsumsjon
) : SammensattSubsumsjon(navn, mutableListOf(child), gyldigSubsumsjon, ugyldigSubsumsjon) {

    internal constructor(navn: String, child: Subsumsjon) : this(navn, child, TomSubsumsjon, TomSubsumsjon)

    override fun deepCopy(søknadprosess: Søknadprosess) = DeltreSubsumsjon(
        navn,
        child.deepCopy(søknadprosess),
        gyldigSubsumsjon.deepCopy(søknadprosess),
        ugyldigSubsumsjon.deepCopy(søknadprosess)
    )

    override fun bygg(søknad: Søknad) = DeltreSubsumsjon(
        navn,
        child.bygg(søknad),
        gyldigSubsumsjon.bygg(søknad),
        ugyldigSubsumsjon.bygg(søknad)
    )

    override fun deepCopy(): Subsumsjon {
        return DeltreSubsumsjon(
            navn,
            child.deepCopy(),
            gyldigSubsumsjon.deepCopy(),
            ugyldigSubsumsjon.deepCopy()
        )
    }

    override fun deepCopy(indeks: Int, søknad: Søknad): Subsumsjon {
        return DeltreSubsumsjon(
            "$navn [$indeks]",
            child.deepCopy(indeks, søknad),
            gyldigSubsumsjon.deepCopy(indeks, søknad),
            ugyldigSubsumsjon.deepCopy(indeks, søknad)
        )
    }

    override fun accept(visitor: SubsumsjonVisitor) {
        resultat().also {
            visitor.preVisit(this, lokaltResultat(), it)
            super.accept(visitor)
            visitor.postVisit(this, lokaltResultat(), it)
        }
    }

    override fun lokaltResultat() = child.resultat()

    override fun _mulige() = this.also {
        child._mulige()
    }
}