package no.nav.dagpenger.qamodel.subsumsjon

import no.nav.dagpenger.qamodel.fakta.Faktum
import no.nav.dagpenger.qamodel.regel.Regel
import no.nav.dagpenger.qamodel.visitor.PrettyPrint
import no.nav.dagpenger.qamodel.visitor.SubsumsjonVisitor

class Subsumsjon(private val regel: Regel, vararg fakta: Faktum<*>) {
    private val fakta = fakta.toList()
    fun konkluder() = regel.konkluder(fakta)

    internal fun accept(visitor: SubsumsjonVisitor) {
        visitor.preVisit(this, regel)
        fakta.forEach { it.accept(visitor) }
        visitor.postVisit(this, regel)
    }

    override fun toString() = PrettyPrint(this).result()
}
