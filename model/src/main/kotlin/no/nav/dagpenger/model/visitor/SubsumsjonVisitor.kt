package no.nav.dagpenger.model.visitor

import no.nav.dagpenger.model.fakta.Faktum
import no.nav.dagpenger.model.fakta.Faktum.FaktumTilstand
import no.nav.dagpenger.model.fakta.GrunnleggendeFaktum
import no.nav.dagpenger.model.fakta.UtledetFaktum
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
    fun <R : Comparable<R>> visit(faktum: GrunnleggendeFaktum<R>, tilstand: FaktumTilstand) {}
    fun <R : Comparable<R>> visit(faktum: GrunnleggendeFaktum<R>, tilstand: FaktumTilstand, svar: R) {}
    fun <R : Comparable<R>> preVisit(faktum: UtledetFaktum<R>, svar: R) {}
    fun <R : Comparable<R>> preVisit(faktum: UtledetFaktum<R>) {}
    fun <R : Comparable<R>> postVisit(faktum: UtledetFaktum<R>) {}
    fun <R : Comparable<R>> preVisit(fakta: Set<Faktum<R>>) {}
    fun <R : Comparable<R>> postVisit(fakta: Set<Faktum<R>>) {}
}
