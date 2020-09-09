package no.nav.dagpenger.qamodel.visitor

import no.nav.dagpenger.qamodel.fakta.Faktum
import no.nav.dagpenger.qamodel.fakta.Faktum.FaktumTilstand
import java.time.LocalDate

interface FaktumVisitor {
    fun preVisitJaNei(faktum: Faktum<Boolean>, tilstand: FaktumTilstand) {}
    fun postVisitJaNei(faktum: Faktum<Boolean>, tilstand: FaktumTilstand) {}
    fun preVisitDato(faktum: Faktum<LocalDate>, tilstand: FaktumTilstand) {}
    fun postVisitDato(faktum: Faktum<LocalDate>, tilstand: FaktumTilstand) {}
}
