package no.nav.dagpenger.qamodel.subsumsjon

import no.nav.dagpenger.qamodel.fakta.Faktum
import no.nav.dagpenger.qamodel.visitor.PrettyPrint
import no.nav.dagpenger.qamodel.visitor.SubsumsjonVisitor

class AlleSubsumsjon internal constructor(
    override val navn: String,
    private val subsumsjoner: List<Subsumsjon>
) : Subsumsjon {
    override lateinit var gyldigSubsumsjon: Subsumsjon
    override fun konkluder() = subsumsjoner.all { it.konkluder() }

    override fun accept(visitor: SubsumsjonVisitor) {
        visitor.preVisit(this)
        subsumsjoner.forEach { it.accept(visitor) }
        if (::gyldigSubsumsjon.isInitialized) acceptGyldig(visitor)
        visitor.postVisit(this)
    }

    override fun nesteFakta(): Set<Faktum<*>> = subsumsjoner.flatMap { it.fakta() }.toSet()

    override fun fakta() = this.nesteFakta() + if (::gyldigSubsumsjon.isInitialized) gyldigSubsumsjon.fakta() else emptySet()

    override fun toString() = PrettyPrint(this).result()
}
