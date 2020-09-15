package no.nav.dagpenger.model.subsumsjon

import no.nav.dagpenger.model.visitor.SubsumsjonVisitor

class AlleSubsumsjon internal constructor(
        navn: String,
        subsumsjoner: List<Subsumsjon>,
        private val handling: Handling
) : SammensattSubsumsjon(navn, subsumsjoner) {
    override fun konkluder() = subsumsjoner.all { it.konkluder() }.also { handling.kjør(this, it) }

    override fun accept(visitor: SubsumsjonVisitor) {
        visitor.preVisit(this)
        super.accept(visitor)
        visitor.postVisit(this)
    }
}
