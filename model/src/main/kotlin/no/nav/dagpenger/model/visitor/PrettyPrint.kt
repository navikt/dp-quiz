package no.nav.dagpenger.model.visitor

import no.nav.dagpenger.model.factory.FaktaRegel
import no.nav.dagpenger.model.faktum.Faktum
import no.nav.dagpenger.model.faktum.Faktum.FaktumTilstand
import no.nav.dagpenger.model.faktum.GrunnleggendeFaktum
import no.nav.dagpenger.model.faktum.GyldigeValg
import no.nav.dagpenger.model.faktum.LandGrupper
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.faktum.UtledetFaktum
import no.nav.dagpenger.model.regel.Regel
import no.nav.dagpenger.model.subsumsjon.AlleSubsumsjon
import no.nav.dagpenger.model.subsumsjon.DeltreSubsumsjon
import no.nav.dagpenger.model.subsumsjon.EnkelSubsumsjon
import no.nav.dagpenger.model.subsumsjon.GodkjenningsSubsumsjon
import no.nav.dagpenger.model.subsumsjon.MinstEnAvSubsumsjon
import no.nav.dagpenger.model.subsumsjon.Subsumsjon
import no.nav.dagpenger.model.subsumsjon.TomSubsumsjon

internal class PrettyPrint(subsumsjon: Subsumsjon) : SubsumsjonVisitor {
    private var result = ""
    private var indentTeller = 0

    init {
        subsumsjon.accept(this)
    }

    fun result() = result

    override fun preVisit(subsumsjon: EnkelSubsumsjon, regel: Regel, fakta: List<Faktum<*>>, lokaltResultat: Boolean?, resultat: Boolean?) {
        melding("${status(resultat)} $subsumsjon")
        indentTeller++
    }

    override fun postVisit(subsumsjon: EnkelSubsumsjon, regel: Regel, fakta: List<Faktum<*>>, lokaltResultat: Boolean?, resultat: Boolean?) {
        indentTeller--
    }

    override fun preVisit(subsumsjon: AlleSubsumsjon, subsumsjoner: List<Subsumsjon>, lokaltResultat: Boolean?, resultat: Boolean?) {
        melding("${status(resultat)} Kombinasjon av subsumsjoner ${subsumsjon.navn}")
        indentTeller++
    }

    override fun postVisit(subsumsjon: AlleSubsumsjon, subsumsjoner: List<Subsumsjon>, lokaltResultat: Boolean?, resultat: Boolean?) {
        indentTeller--
    }

    override fun preVisit(subsumsjon: MinstEnAvSubsumsjon, subsumsjoner: List<Subsumsjon>, lokaltResultat: Boolean?, resultat: Boolean?) {
        melding("${status(resultat)} Kombinasjon av subsumsjoner ${subsumsjon.navn}")
        indentTeller++
    }

    override fun preVisit(subsumsjon: DeltreSubsumsjon, child: Subsumsjon, lokaltResultat: Boolean?, resultat: Boolean?) {
        melding("${status(resultat)} Resultat av subsumsjon ${subsumsjon.navn}")
        indentTeller++
    }

    override fun postVisit(subsumsjon: DeltreSubsumsjon, child: Subsumsjon, lokaltResultat: Boolean?, resultat: Boolean?) {
        indentTeller--
    }

    override fun preVisit(
        subsumsjon: GodkjenningsSubsumsjon,
        action: GodkjenningsSubsumsjon.Action,
        godkjenning: List<GrunnleggendeFaktum<Boolean>>,
        lokaltResultat: Boolean?,
        childResultat: Boolean?
    ) {
        melding("${status(lokaltResultat)} Resultat av subsumsjon ${subsumsjon.navn}")
        indentTeller++
    }
    override fun postVisit(
        subsumsjon: GodkjenningsSubsumsjon,
        action: GodkjenningsSubsumsjon.Action,
        godkjenning: List<GrunnleggendeFaktum<Boolean>>,
        resultat: Boolean?,
        childResultat: Boolean?
    ) {
        indentTeller--
    }

    override fun <R : Comparable<R>> preVisit(
        faktum: UtledetFaktum<R>,
        id: String,
        avhengigeFakta: Set<Faktum<*>>,
        avhengerAvFakta: Set<Faktum<*>>,
        children: Set<Faktum<*>>,
        clazz: Class<R>,
        regel: FaktaRegel<R>,
        svar: R
    ) {
        melding("Faktum: $faktum er utledet til $svar")
        indentTeller++
    }

    override fun <R : Comparable<R>> preVisit(
        faktum: UtledetFaktum<R>,
        id: String,
        avhengigeFakta: Set<Faktum<*>>,
        avhengerAvFakta: Set<Faktum<*>>,
        children: Set<Faktum<*>>,
        clazz: Class<R>,
        regel: FaktaRegel<R>
    ) {
        melding("Faktum: $faktum er ubesvart")
        indentTeller++
    }

    override fun postVisit(subsumsjon: MinstEnAvSubsumsjon, subsumsjoner: List<Subsumsjon>, lokaltResultat: Boolean?, resultat: Boolean?) {
        indentTeller--
    }

    override fun <R : Comparable<R>> postVisit(
        faktum: UtledetFaktum<R>,
        id: String,
        children: Set<Faktum<*>>,
        clazz: Class<R>
    ) {
        indentTeller--
    }

    override fun preVisitOppfylt(parent: Subsumsjon, child: Subsumsjon) {
        if (child is TomSubsumsjon) return

        indentTeller--
        melding(">>Hvis ${parent.navn} er oppfylt: ")
        indentTeller++
    }

    override fun postVisitOppfylt(parent: Subsumsjon, child: Subsumsjon) {} // Tom med vilje

    override fun preVisitIkkeOppfylt(parent: Subsumsjon, child: Subsumsjon) {
        if (child is TomSubsumsjon) return

        indentTeller--
        melding("||Hvis ${parent.navn} ikke er oppfylt: ")
        indentTeller++
    }

    override fun postVisitIkkeOppfylt(parent: Subsumsjon, child: Subsumsjon) {} // Tom med vilje

    override fun <R : Comparable<R>> visitUtenSvar(
        faktum: GrunnleggendeFaktum<R>,
        tilstand: FaktumTilstand,
        id: String,
        avhengigeFakta: Set<Faktum<*>>,
        avhengerAvFakta: Set<Faktum<*>>,
        godkjenner: Set<Faktum<*>>,
        sannsynliggjøringsFakta: Set<Faktum<*>>,
        roller: Set<Rolle>,
        clazz: Class<R>,
        gyldigeValg: GyldigeValg?,
        landGrupper: LandGrupper?
    ) {
        melding("Faktum: $faktum for roller ${roller.joinToString(" og ") { it.typeNavn }} er ubesvart")
    }

    override fun <R : Comparable<R>> visitMedSvar(
        faktum: GrunnleggendeFaktum<R>,
        tilstand: FaktumTilstand,
        id: String,
        avhengigeFakta: Set<Faktum<*>>,
        avhengerAvFakta: Set<Faktum<*>>,
        godkjenner: Set<Faktum<*>>,
        sannsynliggjøringsFakta: Set<Faktum<*>>,
        roller: Set<Rolle>,
        clazz: Class<R>,
        svar: R,
        besvartAv: String?,
        gyldigeValg: GyldigeValg?,
        landGrupper: LandGrupper?
    ) {
        melding("Faktum: $faktum for roller ${roller.joinToString(" og ") { it.typeNavn }} er besvart med $svar")
    }

    private fun melding(navn: String) {
        result += "  ".repeat(indentTeller) + "${navn}\n"
    }

    private fun status(resultat: Boolean?) = when (resultat) {
        true -> "[bestått]"
        false -> "[mislyktes]"
        null -> "[ukjent]"
    }
}
