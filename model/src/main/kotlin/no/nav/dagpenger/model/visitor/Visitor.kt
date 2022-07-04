package no.nav.dagpenger.model.visitor

import no.nav.dagpenger.model.factory.FaktaRegel
import no.nav.dagpenger.model.faktum.Faktum
import no.nav.dagpenger.model.faktum.FaktumId
import no.nav.dagpenger.model.faktum.GeneratorFaktum
import no.nav.dagpenger.model.faktum.GrunnleggendeFaktum
import no.nav.dagpenger.model.faktum.GyldigeValg
import no.nav.dagpenger.model.faktum.Identer
import no.nav.dagpenger.model.faktum.LandGrupper
import no.nav.dagpenger.model.faktum.Person
import no.nav.dagpenger.model.faktum.Prosessversjon
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.faktum.TemplateFaktum
import no.nav.dagpenger.model.faktum.UtledetFaktum
import no.nav.dagpenger.model.regel.Regel
import no.nav.dagpenger.model.seksjon.Seksjon
import no.nav.dagpenger.model.seksjon.Søknadprosess
import no.nav.dagpenger.model.subsumsjon.AlleSubsumsjon
import no.nav.dagpenger.model.subsumsjon.BareEnAvSubsumsjon
import no.nav.dagpenger.model.subsumsjon.DeltreSubsumsjon
import no.nav.dagpenger.model.subsumsjon.EnkelSubsumsjon
import no.nav.dagpenger.model.subsumsjon.GeneratorSubsumsjon
import no.nav.dagpenger.model.subsumsjon.GodkjenningsSubsumsjon
import no.nav.dagpenger.model.subsumsjon.MinstEnAvSubsumsjon
import no.nav.dagpenger.model.subsumsjon.Subsumsjon
import java.util.UUID

interface FaktumVisitor {
    fun <R : Comparable<R>> visitUtenSvar(
        faktum: GrunnleggendeFaktum<R>,
        tilstand: Faktum.FaktumTilstand,
        id: String,
        avhengigeFakta: Set<Faktum<*>>,
        avhengerAvFakta: Set<Faktum<*>>,
        godkjenner: Set<Faktum<*>>,
        roller: Set<Rolle>,
        clazz: Class<R>,
        gyldigeValg: GyldigeValg?,
        landGrupper: LandGrupper?
    ) {
    }

    fun <R : Comparable<R>> visitMedSvar(
        faktum: GrunnleggendeFaktum<R>,
        tilstand: Faktum.FaktumTilstand,
        id: String,
        avhengigeFakta: Set<Faktum<*>>,
        avhengerAvFakta: Set<Faktum<*>>,
        godkjenner: Set<Faktum<*>>,
        roller: Set<Rolle>,
        clazz: Class<R>,
        svar: R,
        besvartAv: String?,
        gyldigeValg: GyldigeValg?,
        landGrupper: LandGrupper?
    ) {
    }

    fun <R : Comparable<R>> visit(
        faktum: TemplateFaktum<R>,
        id: String,
        avhengigeFakta: Set<Faktum<*>>,
        avhengerAvFakta: Set<Faktum<*>>,
        roller: Set<Rolle>,
        clazz: Class<R>,
        gyldigeValg: GyldigeValg?
    ) {
    }

    fun <R : Comparable<R>> visitUtenSvar(
        faktum: GeneratorFaktum,
        id: String,
        avhengigeFakta: Set<Faktum<*>>,
        avhengerAvFakta: Set<Faktum<*>>,
        templates: List<TemplateFaktum<*>>,
        roller: Set<Rolle>,
        clazz: Class<R>
    ) {
    }

    fun <R : Comparable<R>> visitMedSvar(
        faktum: GeneratorFaktum,
        id: String,
        avhengigeFakta: Set<Faktum<*>>,
        avhengerAvFakta: Set<Faktum<*>>,
        templates: List<TemplateFaktum<*>>,
        roller: Set<Rolle>,
        clazz: Class<R>,
        svar: R,
        genererteFaktum: Set<Faktum<*>>
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

interface IdentVisitor {
    fun visit(type: Identer.Ident.Type, id: String, historisk: Boolean) {}
}

interface PersonVisitor : IdentVisitor {
    fun preVisit(person: Person, uuid: UUID) {}
    fun postVisit(person: Person, uuid: UUID) {}
}

interface SøknadVisitor : PersonVisitor, FaktumVisitor {
    fun preVisit(søknad: Søknad, prosessVersjon: Prosessversjon, uuid: UUID) {}
    fun postVisit(søknad: Søknad, prosessVersjon: Prosessversjon, uuid: UUID) {}
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
    fun preVisit(
        subsumsjon: EnkelSubsumsjon,
        regel: Regel,
        fakta: List<Faktum<*>>,
        lokaltResultat: Boolean?,
        resultat: Boolean?
    ) {
    }

    fun postVisit(
        subsumsjon: EnkelSubsumsjon,
        regel: Regel,
        fakta: List<Faktum<*>>,
        lokaltResultat: Boolean?,
        resultat: Boolean?
    ) {
    }

    fun preVisit(subsumsjon: GeneratorSubsumsjon, deltre: DeltreSubsumsjon) {}
    fun postVisit(subsumsjon: GeneratorSubsumsjon, deltre: DeltreSubsumsjon) {}

    fun preVisit(subsumsjon: AlleSubsumsjon, subsumsjoner: List<Subsumsjon>, lokaltResultat: Boolean?, resultat: Boolean?) {}
    fun postVisit(subsumsjon: AlleSubsumsjon, subsumsjoner: List<Subsumsjon>, lokaltResultat: Boolean?, resultat: Boolean?) {}
    fun preVisit(subsumsjon: MinstEnAvSubsumsjon, subsumsjoner: List<Subsumsjon>, lokaltResultat: Boolean?, resultat: Boolean?) {}
    fun postVisit(subsumsjon: MinstEnAvSubsumsjon, subsumsjoner: List<Subsumsjon>, lokaltResultat: Boolean?, resultat: Boolean?) {}
    fun preVisit(subsumsjon: BareEnAvSubsumsjon, lokaltResultat: Boolean?, resultat: Boolean?) {}
    fun postVisit(subsumsjon: BareEnAvSubsumsjon, lokaltResultat: Boolean?, resultat: Boolean?) {}
    fun preVisit(subsumsjon: DeltreSubsumsjon, child: Subsumsjon, lokaltResultat: Boolean?, resultat: Boolean?) {}
    fun postVisit(subsumsjon: DeltreSubsumsjon, child: Subsumsjon, lokaltResultat: Boolean?, resultat: Boolean?) {}
    fun preVisit(
        subsumsjon: GodkjenningsSubsumsjon,
        action: GodkjenningsSubsumsjon.Action,
        godkjenning: List<GrunnleggendeFaktum<Boolean>>,
        lokaltResultat: Boolean?,
        childResultat: Boolean?
    ) {
    }

    fun postVisit(
        subsumsjon: GodkjenningsSubsumsjon,
        action: GodkjenningsSubsumsjon.Action,
        godkjenning: List<GrunnleggendeFaktum<Boolean>>,
        resultat: Boolean?,
        childResultat: Boolean?
    ) {
    }

    fun preVisit(subsumsjon: GodkjenningsSubsumsjon, action: GodkjenningsSubsumsjon.Action, lokaltResultat: Boolean?) {}
    fun postVisit(
        subsumsjon: GodkjenningsSubsumsjon,
        action: GodkjenningsSubsumsjon.Action,
        lokaltResultat: Boolean?
    ) {
    }

    fun preVisitOppfylt(parent: Subsumsjon, child: Subsumsjon) {}
    fun postVisitOppfylt(parent: Subsumsjon, child: Subsumsjon) {}
    fun preVisitIkkeOppfylt(parent: Subsumsjon, child: Subsumsjon) {}
    fun postVisitIkkeOppfylt(parent: Subsumsjon, child: Subsumsjon) {}
}
