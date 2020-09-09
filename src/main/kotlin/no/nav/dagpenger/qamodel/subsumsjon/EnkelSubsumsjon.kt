package no.nav.dagpenger.qamodel.subsumsjon

import no.nav.dagpenger.qamodel.fakta.Faktum
import no.nav.dagpenger.qamodel.regel.Regel
import no.nav.dagpenger.qamodel.visitor.PrettyPrint
import no.nav.dagpenger.qamodel.visitor.SubsumsjonVisitor

class EnkelSubsumsjon internal constructor(
    private val regel: Regel,
    vararg fakta: Faktum<*>
) : Subsumsjon {
    private val fakta = fakta.toList()
    override lateinit var gyldigSubsumsjon: Subsumsjon

    override fun konkluder() = regel.konkluder(fakta)

    override fun accept(visitor: SubsumsjonVisitor) {
        visitor.preVisit(this, regel)
        fakta.forEach { it.accept(visitor) }
        visitor.postVisit(this, regel)
    }

    override fun fakta(): Set<Faktum<*>> = fakta.toSet()

    override fun toString() = PrettyPrint(this).result()
}
