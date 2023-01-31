package no.nav.dagpenger.model.subsumsjon

import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.seksjon.Faktagrupper
import no.nav.dagpenger.model.visitor.SubsumsjonVisitor

// Evalueres til resultatet av det underliggende subsumsjonstreet
class DeltreSubsumsjon private constructor(
    navn: String,
    private val child: Subsumsjon,
    oppfyltSubsumsjon: Subsumsjon,
    ikkeOppfyltSubsumsjon: Subsumsjon
) : SammensattSubsumsjon(navn, mutableListOf(child), oppfyltSubsumsjon, ikkeOppfyltSubsumsjon) {

    internal constructor(navn: String, child: Subsumsjon) : this(navn, child, TomSubsumsjon, TomSubsumsjon)

    override fun deepCopy(faktagrupper: Faktagrupper) = DeltreSubsumsjon(
        navn,
        child.deepCopy(faktagrupper),
        oppfyltSubsumsjon.deepCopy(faktagrupper),
        ikkeOppfyltSubsumsjon.deepCopy(faktagrupper)
    )

    override fun bygg(søknad: Søknad) = DeltreSubsumsjon(
        navn,
        child.bygg(søknad),
        oppfyltSubsumsjon.bygg(søknad),
        ikkeOppfyltSubsumsjon.bygg(søknad)
    )

    override fun deepCopy(): Subsumsjon {
        return DeltreSubsumsjon(
            navn,
            child.deepCopy(),
            oppfyltSubsumsjon.deepCopy(),
            ikkeOppfyltSubsumsjon.deepCopy()
        )
    }

    override fun deepCopy(indeks: Int, søknad: Søknad): Subsumsjon {
        return DeltreSubsumsjon(
            "$navn [$indeks]",
            child.deepCopy(indeks, søknad),
            oppfyltSubsumsjon.deepCopy(indeks, søknad),
            ikkeOppfyltSubsumsjon.deepCopy(indeks, søknad)
        )
    }

    override fun accept(visitor: SubsumsjonVisitor) {
        resultat().also {
            visitor.preVisit(this, child, lokaltResultat(), it)
            super.accept(visitor)
            visitor.postVisit(this, child, lokaltResultat(), it)
        }
    }

    override fun lokaltResultat() = child.resultat()

    override fun _mulige() = this.also {
        child._mulige()
    }
}
