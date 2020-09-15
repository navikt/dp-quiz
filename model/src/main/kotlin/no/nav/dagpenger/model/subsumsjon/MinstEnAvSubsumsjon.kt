package no.nav.dagpenger.model.subsumsjon

import no.nav.dagpenger.model.visitor.SubsumsjonVisitor

class MinstEnAvSubsumsjon internal constructor(
    navn: String,
    subsumsjoner: List<Subsumsjon>
) : SammensattSubsumsjon(navn, subsumsjoner) {
    override fun konkluder() = subsumsjoner.any { it.konkluder() }

    override fun accept(visitor: SubsumsjonVisitor) {
        visitor.preVisit(this)
        super.accept(visitor)
        visitor.postVisit(this)
    }
}
