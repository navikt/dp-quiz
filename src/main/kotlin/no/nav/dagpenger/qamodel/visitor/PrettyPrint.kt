package no.nav.dagpenger.qamodel.visitor

import java.time.LocalDate
import no.nav.dagpenger.qamodel.fakta.Faktum
import no.nav.dagpenger.qamodel.handling.Handling

class PrettyPrint(faktum: Faktum<*>) : FaktumVisitor {
    private var result = ""
    private var indentTeller = 0

    init {
        faktum.accept(this)
    }

    fun result() = result

    override fun preVisitJaNei(faktum: Faktum<Boolean>, tilstand: Faktum.FaktumTilstand) {
        preVisit(faktum.navn)
    }

    override fun postVisitJaNei(faktum: Faktum<Boolean>, tilstand: Faktum.FaktumTilstand) {
        indentTeller--
    }

    override fun preVisitDato(faktum: Faktum<LocalDate>, tilstand: Faktum.FaktumTilstand) {
        preVisit(faktum.navn)
    }

    override fun postVisitDato(faktum: Faktum<LocalDate>, tilstand: Faktum.FaktumTilstand) {
        indentTeller--
    }

    override fun preVisitJa(handling: Handling) {
        preVisit("Ja-handling")
    }

    override fun postVisitJa(handling: Handling) {
        indentTeller--
    }

    override fun preVisitNei(handling: Handling) {
        preVisit("Nei-handling")
    }

    override fun postVisitNei(handling: Handling) {
        indentTeller--
    }

    override fun preVisitDato(handling: Handling) {
        preVisit("Dato-handling")
    }

    override fun postVisitDato(handling: Handling) {
        indentTeller--
    }

    private fun preVisit(navn: String) {
        result += "  ".repeat(indentTeller) + "${navn}\n"
        indentTeller++
    }
}
