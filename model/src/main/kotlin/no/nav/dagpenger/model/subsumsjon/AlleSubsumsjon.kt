package no.nav.dagpenger.model.subsumsjon

import no.nav.dagpenger.model.fakta.Fakta
import no.nav.dagpenger.model.søknad.Søknad
import no.nav.dagpenger.model.visitor.SubsumsjonVisitor

class AlleSubsumsjon private constructor(
    navn: String,
    subsumsjoner: MutableList<Subsumsjon>,
    gyldigSubsumsjon: Subsumsjon,
    ugyldigSubsumsjon: Subsumsjon
) : SammensattSubsumsjon(navn, subsumsjoner, gyldigSubsumsjon, ugyldigSubsumsjon) {

    internal constructor(navn: String, subsumsjoner: List<Subsumsjon>) : this(navn, subsumsjoner.toMutableList(), TomSubsumsjon, TomSubsumsjon)

    override fun deepCopy(søknad: Søknad) = AlleSubsumsjon(
        navn,
        subsumsjoner.map { it.deepCopy(søknad) }.toMutableList(),
        gyldigSubsumsjon.deepCopy(søknad),
        ugyldigSubsumsjon.deepCopy(søknad)
    )

    override fun bygg(fakta: Fakta) = AlleSubsumsjon(
        navn,
        subsumsjoner.map { it.bygg(fakta) }.toMutableList(),
        gyldigSubsumsjon.bygg(fakta),
        ugyldigSubsumsjon.bygg(fakta)
    )

    override fun deepCopy(indeks: Int, fakta: Fakta): Subsumsjon {
        return AlleSubsumsjon(
            "$navn [$indeks]",
            subsumsjoner.map { it.deepCopy(indeks, fakta) }.toMutableList(),
            gyldigSubsumsjon.deepCopy(indeks, fakta),
            ugyldigSubsumsjon.deepCopy(indeks, fakta)
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
