package no.nav.dagpenger.model.subsumsjon

import no.nav.dagpenger.model.søknad.Søknad
import no.nav.dagpenger.model.visitor.SubsumsjonVisitor

class MinstEnAvSubsumsjon private constructor(
    navn: String,
    subsumsjoner: List<Subsumsjon>,
    gyldigSubsumsjon: Subsumsjon,
    ugyldigSubsumsjon: Subsumsjon
) : SammensattSubsumsjon(navn, subsumsjoner.toMutableList(), gyldigSubsumsjon, ugyldigSubsumsjon) {

    internal constructor(navn: String, subsumsjoner: List<Subsumsjon>) : this(navn, subsumsjoner, TomSubsumsjon, TomSubsumsjon)

    override fun deepCopy(søknad: Søknad) = MinstEnAvSubsumsjon(
        navn,
        subsumsjoner.map { it.deepCopy(søknad) },
        gyldigSubsumsjon.deepCopy(søknad),
        ugyldigSubsumsjon.deepCopy(søknad)
    ).also {
        it.søknad = søknad
    }

    override fun deepCopy(indeks: Int): Subsumsjon {
        return MinstEnAvSubsumsjon(
            "$navn [$indeks]",
            subsumsjoner.map { it.deepCopy(indeks) }.toMutableList(),
            gyldigSubsumsjon.deepCopy(indeks),
            ugyldigSubsumsjon.deepCopy(indeks)
        )
    }

    override fun deepCopy(): Subsumsjon {
        return MinstEnAvSubsumsjon(
            navn,
            subsumsjoner.map { it.deepCopy() },
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
