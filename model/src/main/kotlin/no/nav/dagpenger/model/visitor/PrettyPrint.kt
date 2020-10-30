package no.nav.dagpenger.model.visitor

import no.nav.dagpenger.model.factory.FaktaRegel
import no.nav.dagpenger.model.faktum.Faktum
import no.nav.dagpenger.model.faktum.Faktum.FaktumTilstand
import no.nav.dagpenger.model.faktum.GrunnleggendeFaktum
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.faktum.UtledetFaktum
import no.nav.dagpenger.model.regel.Regel
import no.nav.dagpenger.model.subsumsjon.AlleSubsumsjon
import no.nav.dagpenger.model.subsumsjon.EnkelSubsumsjon
import no.nav.dagpenger.model.subsumsjon.MakroSubsumsjon
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

    override fun preVisit(subsumsjon: EnkelSubsumsjon, regel: Regel, fakta: Set<Faktum<*>>, resultat: Boolean?) {
        melding("${status(resultat)} $subsumsjon")
        indentTeller++
    }

    override fun postVisit(subsumsjon: EnkelSubsumsjon, regel: Regel, fakta: Set<Faktum<*>>, resultat: Boolean?) {
        indentTeller--
    }

    override fun preVisit(subsumsjon: AlleSubsumsjon, resultat: Boolean?) {
        melding("${status(resultat)} Kombinasjon av subsumsjoner ${subsumsjon.navn}")
        indentTeller++
    }

    override fun postVisit(subsumsjon: AlleSubsumsjon, resultat: Boolean?) {
        indentTeller--
    }

    override fun preVisit(subsumsjon: MinstEnAvSubsumsjon, resultat: Boolean?) {
        melding("${status(resultat)} Kombinasjon av subsumsjoner ${subsumsjon.navn}")
        indentTeller++
    }

    override fun preVisit(subsumsjon: MakroSubsumsjon, resultat: Boolean?) {
        melding("${status(resultat)} Resultat av subsumsjon ${subsumsjon.navn}")
        indentTeller++
    }

    override fun postVisit(subsumsjon: MakroSubsumsjon, resultat: Boolean?) {
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

    override fun postVisit(subsumsjon: MinstEnAvSubsumsjon, resultat: Boolean?) {
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

    override fun preVisitGyldig(parent: Subsumsjon, child: Subsumsjon) {
        if (child is TomSubsumsjon) return

        indentTeller--
        melding(">>Hvis ${parent.navn} er gyldig: ")
        indentTeller++
    }

    override fun postVisitGyldig(parent: Subsumsjon, child: Subsumsjon) {} // Tom med vilje

    override fun preVisitUgyldig(parent: Subsumsjon, child: Subsumsjon) {
        if (child is TomSubsumsjon) return

        indentTeller--
        melding("||Hvis ${parent.navn} ikke er gyldig: ")
        indentTeller++
    }

    override fun postVisitUgyldig(parent: Subsumsjon, child: Subsumsjon) {} // Tom med vilje

    override fun <R : Comparable<R>> visit(
        faktum: GrunnleggendeFaktum<R>,
        tilstand: FaktumTilstand,
        id: String,
        avhengigeFakta: Set<Faktum<*>>,
        avhengerAvFakta: Set<Faktum<*>>,
        roller: Set<Rolle>,
        clazz: Class<R>
    ) {
        melding("Faktum: $faktum for roller ${roller.joinToString(" og ") { it.name }} er ubesvart")
    }

    override fun <R : Comparable<R>> visit(
        faktum: GrunnleggendeFaktum<R>,
        tilstand: FaktumTilstand,
        id: String,
        avhengigeFakta: Set<Faktum<*>>,
        avhengerAvFakta: Set<Faktum<*>>,
        roller: Set<Rolle>,
        clazz: Class<R>,
        svar: R
    ) {
        melding("Faktum: $faktum for roller ${roller.joinToString(" og ") { it.name }} er besvart med $svar")
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
