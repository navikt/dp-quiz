package no.nav.dagpenger.qamodel.visitor

import no.nav.dagpenger.qamodel.fakta.Faktum
import no.nav.dagpenger.qamodel.regel.Regel
import no.nav.dagpenger.qamodel.subsumsjon.AllSubsumsjon
import no.nav.dagpenger.qamodel.subsumsjon.EnkelSubsumsjon

interface SubsumsjonVisitor {
    fun preVisit(subsumsjon: EnkelSubsumsjon, regel: Regel) {}
    fun postVisit(subsumsjon: EnkelSubsumsjon, regel: Regel) {}
    fun preVisit(subsumsjon: AllSubsumsjon) {}
    fun postVisit(subsumsjon: AllSubsumsjon) {}
    fun <R : Any> visit(faktum: Faktum<R>, tilstand: Faktum.FaktumTilstand) {}
    fun <R : Any> visit(faktum: Faktum<R>, tilstand: Faktum.FaktumTilstand, svar: R) {}
}
