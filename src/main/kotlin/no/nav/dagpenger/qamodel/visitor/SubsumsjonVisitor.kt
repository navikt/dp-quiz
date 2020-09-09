package no.nav.dagpenger.qamodel.visitor

import no.nav.dagpenger.qamodel.fakta.Faktum
import no.nav.dagpenger.qamodel.regel.Regel
import no.nav.dagpenger.qamodel.subsumsjon.AlleSubsumsjon
import no.nav.dagpenger.qamodel.subsumsjon.EnkelSubsumsjon
import no.nav.dagpenger.qamodel.subsumsjon.MinstEnAvSubsumsjon

interface SubsumsjonVisitor {
    fun preVisit(subsumsjon: EnkelSubsumsjon, regel: Regel) {}
    fun postVisit(subsumsjon: EnkelSubsumsjon, regel: Regel) {}
    fun preVisit(subsumsjon: AlleSubsumsjon) {}
    fun postVisit(subsumsjon: AlleSubsumsjon) {}
    fun preVisit(subsumsjon: MinstEnAvSubsumsjon) {}
    fun postVisit(subsumsjon: MinstEnAvSubsumsjon) {}
    fun <R : Any> visit(faktum: Faktum<R>, tilstand: Faktum.FaktumTilstand) {}
    fun <R : Any> visit(faktum: Faktum<R>, tilstand: Faktum.FaktumTilstand, svar: R) {}
}
