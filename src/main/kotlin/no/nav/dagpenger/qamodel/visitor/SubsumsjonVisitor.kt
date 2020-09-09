package no.nav.dagpenger.qamodel.visitor

import no.nav.dagpenger.qamodel.fakta.Faktum
import no.nav.dagpenger.qamodel.regel.Regel
import no.nav.dagpenger.qamodel.subsumsjon.Subsumsjon

interface SubsumsjonVisitor {
    fun preVisit(subsumsjon: Subsumsjon, regel: Regel) {}
    fun postVisit(subsumsjon: Subsumsjon, regel: Regel) {}
    fun <R : Any> visit(faktum: Faktum<R>, tilstand: Faktum.FaktumTilstand) {}
    fun <R : Any> visit(faktum: Faktum<R>, tilstand: Faktum.FaktumTilstand, svar: R) {}
}
