package no.nav.dagpenger.model.visitor

import no.nav.dagpenger.model.fakta.Faktum
import no.nav.dagpenger.model.fakta.Faktum.*
import no.nav.dagpenger.model.fakta.GrunnleggendeFaktum
import no.nav.dagpenger.model.fakta.SammensattFaktum
import no.nav.dagpenger.model.regel.Regel
import no.nav.dagpenger.model.subsumsjon.AlleSubsumsjon
import no.nav.dagpenger.model.subsumsjon.EnkelSubsumsjon
import no.nav.dagpenger.model.subsumsjon.MinstEnAvSubsumsjon
import no.nav.dagpenger.model.subsumsjon.Subsumsjon

interface SubsumsjonVisitor {
    fun preVisit(subsumsjon: EnkelSubsumsjon, regel: Regel) {}
    fun postVisit(subsumsjon: EnkelSubsumsjon, regel: Regel) {}
    fun preVisit(subsumsjon: AlleSubsumsjon) {}
    fun postVisit(subsumsjon: AlleSubsumsjon) {}
    fun preVisit(subsumsjon: MinstEnAvSubsumsjon) {}
    fun postVisit(subsumsjon: MinstEnAvSubsumsjon) {}
    fun preVisitGyldig(parent: Subsumsjon, child: Subsumsjon) {}
    fun postVisitGyldig(parent: Subsumsjon, child: Subsumsjon) {}
    fun preVisitUgyldig(parent: Subsumsjon, child: Subsumsjon) {}
    fun postVisitUgyldig(parent: Subsumsjon, child: Subsumsjon) {}
    fun <R : Any> visit(faktum: GrunnleggendeFaktum<R>, tilstand: FaktumTilstand) {}
    fun <R : Any> visit(faktum: GrunnleggendeFaktum<R>, tilstand: FaktumTilstand, svar: R) {}
    fun <R : Any> preVisit(faktum: SammensattFaktum<R>, svar: R)  {}
    fun <R : Any> preVisit(faktum: SammensattFaktum<R>) {}
    fun <R : Any> postVisit(faktum: SammensattFaktum<R>) {}
    fun <R : Any> preVisit(fakta: Set<Faktum<R>>) {}
    fun <R : Any> postVisit(fakta: Set<Faktum<R>>) {}
}
