package no.nav.dagpenger.model.subsumsjon

import no.nav.dagpenger.model.visitor.SubsumsjonVisitor

class AlleSubsumsjon internal constructor(
    navn: String,
    subsumsjoner: List<Subsumsjon>
) : SammensattSubsumsjon(navn, subsumsjoner) {
    override fun konkluder() = subsumsjoner.all { it.konkluder() }

    override fun accept(visitor: SubsumsjonVisitor) {
        visitor.preVisit(this)
        subsumsjoner.forEach { it.accept(visitor) }
        acceptGyldig(visitor)
        acceptUgyldig(visitor)
        visitor.postVisit(this)
    }
}
