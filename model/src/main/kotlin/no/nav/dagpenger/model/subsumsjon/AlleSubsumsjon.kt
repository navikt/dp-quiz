package no.nav.dagpenger.model.subsumsjon

import no.nav.dagpenger.model.fakta.Faktum
import no.nav.dagpenger.model.fakta.FaktumNavn
import no.nav.dagpenger.model.visitor.SubsumsjonVisitor

class AlleSubsumsjon private constructor(
    navn: String,
    subsumsjoner: MutableList<Subsumsjon>,
    gyldigSubsumsjon: Subsumsjon,
    ugyldigSubsumsjon: Subsumsjon
) : SammensattSubsumsjon(navn, subsumsjoner, gyldigSubsumsjon, ugyldigSubsumsjon) {

    internal constructor(navn: String, subsumsjoner: List<Subsumsjon>) : this(navn, subsumsjoner.toMutableList(), TomSubsumsjon, TomSubsumsjon)

    override fun deepCopy(faktaMap: Map<FaktumNavn, Faktum<*>>): Subsumsjon {
        return AlleSubsumsjon(
            navn,
            subsumsjoner.map { it.deepCopy(faktaMap) }.toMutableList(),
            gyldigSubsumsjon.deepCopy(faktaMap),
            ugyldigSubsumsjon.deepCopy(faktaMap)
        )
    }

    override fun deepCopy(indeks: Int): Subsumsjon {
        return AlleSubsumsjon(
            "$navn [$indeks]",
            subsumsjoner.map { it.deepCopy(indeks) }.toMutableList(),
            gyldigSubsumsjon.deepCopy(indeks),
            ugyldigSubsumsjon.deepCopy(indeks)
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
