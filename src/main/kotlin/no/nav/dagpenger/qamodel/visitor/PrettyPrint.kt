package no.nav.dagpenger.qamodel.visitor

import no.nav.dagpenger.qamodel.fakta.Faktum
import no.nav.dagpenger.qamodel.regel.Regel
import no.nav.dagpenger.qamodel.subsumsjon.Subsumsjon

class PrettyPrint(subsumsjon: Subsumsjon) : SubsumsjonVisitor {
    private var result = ""
    private var indentTeller = 0

    init {
        subsumsjon.accept(this)
    }

    fun result() = result

    private fun preVisit(navn: String) {
        result += "  ".repeat(indentTeller) + "${navn}\n"
    }

    override fun preVisit(subsumsjon: Subsumsjon, regel: Regel) {
        preVisit("Regel: ${regel.javaClass.simpleName}")
        indentTeller++
    }

    override fun postVisit(subsumsjon: Subsumsjon, regel: Regel) {
        indentTeller--
    }

    override fun <R : Any> visit(faktum: Faktum<R>, tilstand: Faktum.FaktumTilstand) {
        preVisit("Faktum: ${faktum.navn} er ubesvart")
    }

    override fun <R : Any> visit(faktum: Faktum<R>, tilstand: Faktum.FaktumTilstand, svar: R) {
        preVisit("Faktum: ${faktum.navn} er besvart med $svar")
    }
}
