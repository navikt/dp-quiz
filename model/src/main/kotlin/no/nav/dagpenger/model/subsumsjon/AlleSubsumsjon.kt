package no.nav.dagpenger.model.subsumsjon

import no.nav.dagpenger.model.fakta.Faktum
import no.nav.dagpenger.model.fakta.FaktumNavn
import no.nav.dagpenger.model.visitor.SubsumsjonVisitor

class AlleSubsumsjon private constructor(
    navn: String,
    subsumsjoner: List<Subsumsjon>,
    gyldigSubsumsjon: Subsumsjon,
    ugyldigSubsumsjon: Subsumsjon
) : SammensattSubsumsjon(navn, subsumsjoner, gyldigSubsumsjon, ugyldigSubsumsjon) {

    internal constructor(navn: String, subsumsjoner: List<Subsumsjon>) : this(navn, subsumsjoner, TomSubsumsjon, TomSubsumsjon)

    override fun deepCopy(faktaMap: Map<FaktumNavn, Faktum<*>>): Subsumsjon {
        return AlleSubsumsjon(
            navn,
            subsumsjoner.map { it.deepCopy(faktaMap) },
            gyldigSubsumsjon.deepCopy(faktaMap),
            ugyldigSubsumsjon.deepCopy(faktaMap)
        )
    }

    override fun accept(visitor: SubsumsjonVisitor) {
        visitor.preVisit(this)
        super.accept(visitor)
        visitor.postVisit(this)
    }
}
