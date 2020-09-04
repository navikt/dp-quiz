package no.nav.dagpenger.qamodel.visitor

import no.nav.dagpenger.qamodel.fakta.Faktum
import no.nav.dagpenger.qamodel.fakta.Faktum.FaktumTilstand
import no.nav.dagpenger.qamodel.handling.Handling
import java.time.LocalDate

interface FaktumVisitor {
    fun preVisitJaNei(faktum: Faktum<Boolean>, tilstand: FaktumTilstand) {}
    fun postVisitJaNei(faktum: Faktum<Boolean>, tilstand: FaktumTilstand) {}
    fun preVisitDato(faktum: Faktum<LocalDate>, tilstand: FaktumTilstand) {}
    fun postVisitDato(faktum: Faktum<LocalDate>, tilstand: FaktumTilstand) {}
    fun preVisitJa(handling: Handling) {}
    fun postVisitJa(handling: Handling) {}
    fun preVisitNei(handling: Handling) {}
    fun postVisitNei(handling: Handling) {}
    fun preVisitDato(handling: Handling) {}
    fun postVisitDato(handling: Handling) {}
}
