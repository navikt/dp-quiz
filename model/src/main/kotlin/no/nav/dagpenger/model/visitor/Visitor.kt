package no.nav.dagpenger.model.visitor

import no.nav.dagpenger.model.fakta.Faktum
import no.nav.dagpenger.model.fakta.GrunnleggendeFaktum
import no.nav.dagpenger.model.fakta.Rolle
import no.nav.dagpenger.model.fakta.UtledetFaktum
import no.nav.dagpenger.model.regel.Regel
import no.nav.dagpenger.model.subsumsjon.AlleSubsumsjon
import no.nav.dagpenger.model.subsumsjon.EnkelSubsumsjon
import no.nav.dagpenger.model.subsumsjon.MinstEnAvSubsumsjon
import no.nav.dagpenger.model.subsumsjon.Subsumsjon
import no.nav.dagpenger.model.søknad.Seksjon
import no.nav.dagpenger.model.søknad.Søknad
import java.util.UUID

interface FaktumVisitor {
    fun <R : Comparable<R>> visit(
        faktum: GrunnleggendeFaktum<R>,
        tilstand: Faktum.FaktumTilstand,
        id: Int,
        avhengigeFakta: List<Faktum<*>>,
        roller: Set<Rolle>,
        clazz: Class<R>
    ) {}
    fun <R : Comparable<R>> visit(
        faktum: GrunnleggendeFaktum<R>,
        tilstand: Faktum.FaktumTilstand,
        id: Int,
        avhengigeFakta: List<Faktum<*>>,
        roller: Set<Rolle>,
        clazz: Class<R>,
        svar: R
    ) {}
    fun <R : Comparable<R>> preVisit(
        faktum: UtledetFaktum<R>,
        id: Int,
        avhengigeFakta: List<Faktum<*>>,
        children: Set<Faktum<*>>,
        clazz: Class<R>,
        svar: R
    ) {}
    fun <R : Comparable<R>> preVisit(
        faktum: UtledetFaktum<R>,
        id: Int,
        avhengigeFakta: List<Faktum<*>>,
        children: Set<Faktum<*>>,
        clazz: Class<R>
    ) {}
    fun <R : Comparable<R>> postVisit(
        faktum: UtledetFaktum<R>,
        id: Int,
        children: Set<Faktum<*>>,
        clazz: Class<R>
    ) {}
}

interface SøknadVisitor : FaktumVisitor {
    fun preVisit(søknad: Søknad, uuid: UUID) {}
    fun postVisit(søknad: Søknad) {}
    fun preVisit(seksjon: Seksjon, rolle: Rolle, fakta: Set<Faktum<*>>) {}
    fun postVisit(seksjon: Seksjon, rolle: Rolle) {}
}

interface SubsumsjonVisitor : FaktumVisitor {
    fun preVisit(subsumsjon: EnkelSubsumsjon, regel: Regel, fakta: Set<Faktum<*>>) {}
    fun postVisit(subsumsjon: EnkelSubsumsjon, regel: Regel, fakta: Set<Faktum<*>>) {}
    fun preVisit(subsumsjon: AlleSubsumsjon) {}
    fun postVisit(subsumsjon: AlleSubsumsjon) {}
    fun preVisit(subsumsjon: MinstEnAvSubsumsjon) {}
    fun postVisit(subsumsjon: MinstEnAvSubsumsjon) {}
    fun preVisitGyldig(parent: Subsumsjon, child: Subsumsjon) {}
    fun postVisitGyldig(parent: Subsumsjon, child: Subsumsjon) {}
    fun preVisitUgyldig(parent: Subsumsjon, child: Subsumsjon) {}
    fun postVisitUgyldig(parent: Subsumsjon, child: Subsumsjon) {}
}
