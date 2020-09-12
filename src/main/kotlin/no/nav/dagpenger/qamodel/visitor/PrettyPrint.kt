package no.nav.dagpenger.qamodel.visitor

import no.nav.dagpenger.qamodel.fakta.Faktum
import no.nav.dagpenger.qamodel.regel.Regel
import no.nav.dagpenger.qamodel.subsumsjon.AlleSubsumsjon
import no.nav.dagpenger.qamodel.subsumsjon.EnkelSubsumsjon
import no.nav.dagpenger.qamodel.subsumsjon.MinstEnAvSubsumsjon
import no.nav.dagpenger.qamodel.subsumsjon.Subsumsjon
import no.nav.dagpenger.qamodel.subsumsjon.TomSubsumsjon

internal class PrettyPrint(subsumsjon: Subsumsjon) : SubsumsjonVisitor {
    private var result = ""
    private var indentTeller = 0

    init {
        subsumsjon.accept(this)
    }

    fun result() = result

    override fun preVisit(subsumsjon: EnkelSubsumsjon, regel: Regel) {
        melding("${status(subsumsjon)} $subsumsjon")
        indentTeller++
    }

    override fun postVisit(subsumsjon: EnkelSubsumsjon, regel: Regel) {
        indentTeller--
    }

    override fun preVisit(subsumsjon: AlleSubsumsjon) {
        melding("${status(subsumsjon)} Kombinasjon av subsumsjoner ${subsumsjon.navn}")
        indentTeller++
    }

    override fun postVisit(subsumsjon: AlleSubsumsjon) {
        indentTeller--
    }

    override fun preVisit(subsumsjon: MinstEnAvSubsumsjon) {
        melding("${status(subsumsjon)} Kombinasjon av subsumsjoner ${subsumsjon.navn}")
        indentTeller++
    }

    override fun postVisit(subsumsjon: MinstEnAvSubsumsjon) {
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

    override fun <R : Any> visit(faktum: Faktum<R>, tilstand: Faktum.FaktumTilstand) {
        melding("Faktum: ${faktum.navn} er ubesvart")
    }

    override fun <R : Any> visit(faktum: Faktum<R>, tilstand: Faktum.FaktumTilstand, svar: R) {
        melding("Faktum: ${faktum.navn} er besvart med $svar")
    }

    private fun melding(navn: String) {
        result += "  ".repeat(indentTeller) + "${navn}\n"
    }

    private fun status(subsumsjon: Subsumsjon) = when (subsumsjon.resultat()) {
        true -> "[bestått]"
        false -> "[mislyktes]"
        null -> "[ukjent]"
    }
}
