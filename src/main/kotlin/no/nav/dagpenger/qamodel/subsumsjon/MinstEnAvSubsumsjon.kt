package no.nav.dagpenger.qamodel.subsumsjon

import no.nav.dagpenger.qamodel.fakta.Faktum
import no.nav.dagpenger.qamodel.visitor.PrettyPrint
import no.nav.dagpenger.qamodel.visitor.SubsumsjonVisitor

class MinstEnAvSubsumsjon internal constructor(
    internal val navn: String,
    private val subsumsjoner: List<Subsumsjon>
) : Subsumsjon {
    override fun konkluder() = subsumsjoner.any { it.konkluder() }

    override fun accept(visitor: SubsumsjonVisitor) {
        visitor.preVisit(this)
        subsumsjoner.forEach { it.accept(visitor) }
        visitor.postVisit(this)
    }

    override fun fakta(): Set<Faktum<*>> = subsumsjoner.flatMap { it.fakta() }.toSet()

    override fun toString() = PrettyPrint(this).result()
}
