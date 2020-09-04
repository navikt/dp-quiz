package no.nav.dagpenger.qamodel.visitor

import java.time.LocalDate
import no.nav.dagpenger.qamodel.fakta.Faktum
import no.nav.dagpenger.qamodel.handling.Handling

interface FaktumVisitor {
    fun preVisitJaNei(faktum: Faktum<Boolean>) {}
    fun postVisitJaNei(faktum: Faktum<Boolean>) {}
    fun preVisitDato(faktum: Faktum<LocalDate>) {}
    fun postVisitDato(faktum: Faktum<LocalDate>) {}
    fun preVisitJa(handling: Handling) {}
    fun postVisitJa(handling: Handling) {}
    fun preVisitNei(handling: Handling) {}
    fun postVisitNei(handling: Handling) {}
    fun preVisitDato(handling: Handling) {}
    fun postVisitDato(handling: Handling) {}
}
