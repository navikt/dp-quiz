package no.nav.dagpenger.model.subsumsjon

import no.nav.dagpenger.model.faktagrupper.Faktagrupper
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.visitor.SubsumsjonVisitor

class MakroSubsumsjon private constructor(
    navn: String,
    private val child: Subsumsjon,
    gyldigSubsumsjon: Subsumsjon,
    ugyldigSubsumsjon: Subsumsjon
) : SammensattSubsumsjon(navn, mutableListOf(child), gyldigSubsumsjon, ugyldigSubsumsjon) {

    internal constructor(navn: String, child: Subsumsjon) : this(navn, child, TomSubsumsjon, TomSubsumsjon)

    override fun deepCopy(faktagrupper: Faktagrupper) = MakroSubsumsjon(
        navn,
        child.deepCopy(faktagrupper),
        gyldigSubsumsjon.deepCopy(faktagrupper),
        ugyldigSubsumsjon.deepCopy(faktagrupper)
    )

    override fun bygg(søknad: Søknad) = MakroSubsumsjon(
        navn,
        child.bygg(søknad),
        gyldigSubsumsjon.bygg(søknad),
        ugyldigSubsumsjon.bygg(søknad)
    )

    override fun deepCopy(): Subsumsjon {
        return MakroSubsumsjon(
            navn,
            child.deepCopy(),
            gyldigSubsumsjon.deepCopy(),
            ugyldigSubsumsjon.deepCopy()
        )
    }

    override fun deepCopy(indeks: Int, søknad: Søknad): Subsumsjon {
        return MakroSubsumsjon(
            "$navn [$indeks]",
            child.deepCopy(indeks, søknad),
            gyldigSubsumsjon.deepCopy(indeks, søknad),
            ugyldigSubsumsjon.deepCopy(indeks, søknad)
        )
    }

    override fun accept(visitor: SubsumsjonVisitor) {
        resultat().also {
            visitor.preVisit(this, it)
            super.accept(visitor)
            visitor.postVisit(this, it)
        }
    }

    override fun lokaltResultat() = child.resultat()
}
