package no.nav.dagpenger.model.subsumsjon

import no.nav.dagpenger.model.faktum.Fakta
import no.nav.dagpenger.model.seksjon.Utredningsprosess
import no.nav.dagpenger.model.visitor.SubsumsjonVisitor

// Evalueres til resultatet av det underliggende subsumsjonstreet
class DeltreSubsumsjon private constructor(
    navn: String,
    private val child: Subsumsjon,
    oppfyltSubsumsjon: Subsumsjon,
    ikkeOppfyltSubsumsjon: Subsumsjon
) : SammensattSubsumsjon(navn, mutableListOf(child), oppfyltSubsumsjon, ikkeOppfyltSubsumsjon) {

    internal constructor(navn: String, child: Subsumsjon) : this(navn, child, TomSubsumsjon, TomSubsumsjon)

    override fun deepCopy(utredningsprosess: Utredningsprosess) = DeltreSubsumsjon(
        navn,
        child.deepCopy(utredningsprosess),
        oppfyltSubsumsjon.deepCopy(utredningsprosess),
        ikkeOppfyltSubsumsjon.deepCopy(utredningsprosess)
    )

    override fun bygg(fakta: Fakta) = DeltreSubsumsjon(
        navn,
        child.bygg(fakta),
        oppfyltSubsumsjon.bygg(fakta),
        ikkeOppfyltSubsumsjon.bygg(fakta)
    )

    override fun deepCopy(): Subsumsjon {
        return DeltreSubsumsjon(
            navn,
            child.deepCopy(),
            oppfyltSubsumsjon.deepCopy(),
            ikkeOppfyltSubsumsjon.deepCopy()
        )
    }

    override fun deepCopy(indeks: Int, fakta: Fakta): Subsumsjon {
        return DeltreSubsumsjon(
            "$navn [$indeks]",
            child.deepCopy(indeks, fakta),
            oppfyltSubsumsjon.deepCopy(indeks, fakta),
            ikkeOppfyltSubsumsjon.deepCopy(indeks, fakta)
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
