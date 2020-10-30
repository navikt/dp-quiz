package no.nav.dagpenger.model.subsumsjon

import no.nav.dagpenger.model.faktagrupper.Faktagrupper
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.visitor.SubsumsjonVisitor

class AlleSubsumsjon private constructor(
    navn: String,
    subsumsjoner: MutableList<Subsumsjon>,
    gyldigSubsumsjon: Subsumsjon,
    ugyldigSubsumsjon: Subsumsjon
) : SammensattSubsumsjon(navn, subsumsjoner, gyldigSubsumsjon, ugyldigSubsumsjon) {

    internal constructor(navn: String, subsumsjoner: List<Subsumsjon>) : this(navn, subsumsjoner.toMutableList(), TomSubsumsjon, TomSubsumsjon)

    override fun deepCopy(faktagrupper: Faktagrupper) = AlleSubsumsjon(
        navn,
        subsumsjoner.map { it.deepCopy(faktagrupper) }.toMutableList(),
        gyldigSubsumsjon.deepCopy(faktagrupper),
        ugyldigSubsumsjon.deepCopy(faktagrupper)
    )

    override fun bygg(søknad: Søknad) = AlleSubsumsjon(
        navn,
        subsumsjoner.map { it.bygg(søknad) }.toMutableList(),
        gyldigSubsumsjon.bygg(søknad),
        ugyldigSubsumsjon.bygg(søknad)
    )

    override fun deepCopy(indeks: Int, søknad: Søknad): Subsumsjon {
        return AlleSubsumsjon(
            "$navn [$indeks]",
            subsumsjoner.map { it.deepCopy(indeks, søknad) }.toMutableList(),
            gyldigSubsumsjon.deepCopy(indeks, søknad),
            ugyldigSubsumsjon.deepCopy(indeks, søknad)
        )
    }

    override fun deepCopy(): Subsumsjon {
        return AlleSubsumsjon(
            navn,
            subsumsjoner.map { it.deepCopy() }.toMutableList(),
            gyldigSubsumsjon.deepCopy(),
            ugyldigSubsumsjon.deepCopy()
        )
    }

    override fun accept(visitor: SubsumsjonVisitor) {
        resultat().also {
            visitor.preVisit(this, it)
            super.accept(visitor)
            visitor.postVisit(this, it)
        }
    }
}
