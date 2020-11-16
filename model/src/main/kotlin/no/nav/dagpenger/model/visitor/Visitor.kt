package no.nav.dagpenger.model.visitor

import no.nav.dagpenger.model.factory.FaktaRegel
import no.nav.dagpenger.model.faktum.Faktum
import no.nav.dagpenger.model.faktum.FaktumId
import no.nav.dagpenger.model.faktum.GeneratorFaktum
import no.nav.dagpenger.model.faktum.GrunnleggendeFaktum
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.faktum.TemplateFaktum
import no.nav.dagpenger.model.faktum.UtledetFaktum
import no.nav.dagpenger.model.faktum.ValgFaktum
import no.nav.dagpenger.model.regel.Regel
import no.nav.dagpenger.model.seksjon.Seksjon
import no.nav.dagpenger.model.seksjon.Søknadprosess
import no.nav.dagpenger.model.subsumsjon.AlleSubsumsjon
import no.nav.dagpenger.model.subsumsjon.EnkelSubsumsjon
import no.nav.dagpenger.model.subsumsjon.GodkjenningsSubsumsjon
import no.nav.dagpenger.model.subsumsjon.MakroSubsumsjon
import no.nav.dagpenger.model.subsumsjon.MinstEnAvSubsumsjon
import no.nav.dagpenger.model.subsumsjon.Subsumsjon
import java.util.UUID

interface FaktumVisitor {
    fun <R : Comparable<R>> visit(
        faktum: GrunnleggendeFaktum<R>,
        tilstand: Faktum.FaktumTilstand,
        id: String,
        avhengigeFakta: Set<Faktum<*>>,
        avhengerAvFakta: Set<Faktum<*>>,
        roller: Set<Rolle>,
        clazz: Class<R>
    ) {
    }

    fun <R : Comparable<R>> visit(
        faktum: GrunnleggendeFaktum<R>,
        tilstand: Faktum.FaktumTilstand,
        id: String,
        avhengigeFakta: Set<Faktum<*>>,
        avhengerAvFakta: Set<Faktum<*>>,
        roller: Set<Rolle>,
        clazz: Class<R>,
        svar: R
    ) {
    }

    fun <R : Comparable<R>> visit(
        faktum: TemplateFaktum<R>,
        id: String,
        avhengigeFakta: Set<Faktum<*>>,
        avhengerAvFakta: Set<Faktum<*>>,
        roller: Set<Rolle>,
        clazz: Class<R>
    ) {
    }

    fun <R : Comparable<R>> visit(
        faktum: GeneratorFaktum,
        id: String,
        avhengigeFakta: Set<Faktum<*>>,
        avhengerAvFakta: Set<Faktum<*>>,
        templates: List<Faktum<*>>,
        roller: Set<Rolle>,
        clazz: Class<R>
    ) {
    }

    fun <R : Comparable<R>> visit(
        faktum: GeneratorFaktum,
        id: String,
        avhengigeFakta: Set<Faktum<*>>,
        avhengerAvFakta: Set<Faktum<*>>,
        templates: List<Faktum<*>>,
        roller: Set<Rolle>,
        clazz: Class<R>,
        svar: R
    ) {
    }

    fun <R : Comparable<R>> preVisit(
        faktum: UtledetFaktum<R>,
        id: String,
        avhengigeFakta: Set<Faktum<*>>,
        avhengerAvFakta: Set<Faktum<*>>,
        children: Set<Faktum<*>>,
        clazz: Class<R>,
        regel: FaktaRegel<R>,
        svar: R
    ) {
    }

    fun <R : Comparable<R>> preVisit(
        faktum: UtledetFaktum<R>,
        id: String,
        avhengigeFakta: Set<Faktum<*>>,
        avhengerAvFakta: Set<Faktum<*>>,
        children: Set<Faktum<*>>,
        clazz: Class<R>,
        regel: FaktaRegel<R>
    ) {
    }

    fun <R : Comparable<R>> postVisit(
        faktum: UtledetFaktum<R>,
        id: String,
        children: Set<Faktum<*>>,
        clazz: Class<R>
    ) {
    }

    fun preVisit(
        faktum: ValgFaktum,
        id: String,
        avhengigeFakta: Set<Faktum<*>>,
        avhengerAvFakta: Set<Faktum<*>>,
        underordnedeJa: Set<Faktum<Boolean>>,
        underordnedeNei: Set<Faktum<Boolean>>,
        clazz: Class<Boolean>,
        svar: Boolean
    ) {
    }

    fun preVisit(
        faktum: ValgFaktum,
        id: String,
        avhengigeFakta: Set<Faktum<*>>,
        avhengerAvFakta: Set<Faktum<*>>,
        underordnedeJa: Set<Faktum<Boolean>>,
        underordnedeNei: Set<Faktum<Boolean>>,
        clazz: Class<Boolean>,
    ) {
    }

    fun postVisit(
        faktum: ValgFaktum,
        id: String,
        underordnedeJa: Set<Faktum<Boolean>>,
        underordnedeNei: Set<Faktum<Boolean>>,
        clazz: Class<Boolean>
    ) {
    }

    fun visit(
        faktumId: FaktumId,
        rootId: Int,
        indeks: Int
    ) {
    }

    fun <R : Comparable<R>> preVisitAvhengerAvFakta(faktum: Faktum<R>, avhengerAvFakta: MutableSet<Faktum<*>>) {}
    fun <R : Comparable<R>> postVisitAvhengerAvFakta(faktum: Faktum<R>, avhengerAvFakta: MutableSet<Faktum<*>>) {}
    fun <R : Comparable<R>> preVisitAvhengigeFakta(faktum: Faktum<R>, avhengigeFakta: MutableSet<Faktum<*>>) {}
    fun <R : Comparable<R>> postVisitAvhengigeFakta(faktum: Faktum<R>, avhengigeFakta: MutableSet<Faktum<*>>) {}
}

interface SøknadVisitor : FaktumVisitor {
    fun preVisit(søknad: Søknad, fnr: String, versjonId: Int, uuid: UUID) {}
    fun postVisit(søknad: Søknad, fnr: String, versjonId: Int, uuid: UUID) {}
}

interface SøknadprosessVisitor : SubsumsjonVisitor, SøknadVisitor {
    fun preVisit(søknadprosess: Søknadprosess, uuid: UUID) {}
    fun postVisit(søknadprosess: Søknadprosess) {}
    fun preVisit(seksjon: Seksjon, rolle: Rolle, fakta: Set<Faktum<*>>, indeks: Int) {}
    fun postVisit(seksjon: Seksjon, rolle: Rolle, indeks: Int) {}
    fun preVisitAvhengerAv(seksjon: Seksjon, avhengerAvFakta: Set<Faktum<*>>) {}
    fun postVisitAvhengerAv(seksjon: Seksjon, avhengerAvFakta: Set<Faktum<*>>) {}
}

interface SubsumsjonVisitor : FaktumVisitor {
    fun preVisit(subsumsjon: EnkelSubsumsjon, regel: Regel, fakta: Set<Faktum<*>>, lokaltResultat: Boolean?, resultat: Boolean?) {}
    fun postVisit(subsumsjon: EnkelSubsumsjon, regel: Regel, fakta: Set<Faktum<*>>, lokaltResultat: Boolean?, resultat: Boolean?) {}
    fun preVisit(subsumsjon: AlleSubsumsjon, resultat: Boolean?) {}
    fun postVisit(subsumsjon: AlleSubsumsjon, resultat: Boolean?) {}
    fun preVisit(subsumsjon: MinstEnAvSubsumsjon, resultat: Boolean?) {}
    fun postVisit(subsumsjon: MinstEnAvSubsumsjon, resultat: Boolean?) {}
    fun preVisit(subsumsjon: MakroSubsumsjon, resultat: Boolean?) {}
    fun postVisit(subsumsjon: MakroSubsumsjon, resultat: Boolean?) {}
    fun preVisit(subsumsjon: GodkjenningsSubsumsjon, resultat: Boolean?) {}
    fun postVisit(subsumsjon: GodkjenningsSubsumsjon, resultat: Boolean?) {}
    fun preVisit(subsumsjon: GodkjenningsSubsumsjon, action: GodkjenningsSubsumsjon.Action, lokaltResultat: Boolean?) {}
    fun postVisit(subsumsjon: GodkjenningsSubsumsjon, action: GodkjenningsSubsumsjon.Action, lokaltResultat: Boolean?) {}
    fun preVisitGyldig(parent: Subsumsjon, child: Subsumsjon) {}
    fun postVisitGyldig(parent: Subsumsjon, child: Subsumsjon) {}
    fun preVisitUgyldig(parent: Subsumsjon, child: Subsumsjon) {}
    fun postVisitUgyldig(parent: Subsumsjon, child: Subsumsjon) {}
}
