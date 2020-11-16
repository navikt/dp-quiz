package no.nav.dagpenger.model.subsumsjon

import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.seksjon.Søknadprosess
import no.nav.dagpenger.model.visitor.SubsumsjonVisitor

class MinstEnAvSubsumsjon private constructor(
    navn: String,
    subsumsjoner: List<Subsumsjon>,
    gyldigSubsumsjon: Subsumsjon,
    ugyldigSubsumsjon: Subsumsjon
) : SammensattSubsumsjon(navn, subsumsjoner.toMutableList(), gyldigSubsumsjon, ugyldigSubsumsjon) {

    internal constructor(navn: String, subsumsjoner: List<Subsumsjon>) : this(navn, subsumsjoner, TomSubsumsjon, TomSubsumsjon)

    override fun deepCopy(søknadprosess: Søknadprosess) = MinstEnAvSubsumsjon(
        navn,
        subsumsjoner.map { it.deepCopy(søknadprosess) },
        gyldigSubsumsjon.deepCopy(søknadprosess),
        ugyldigSubsumsjon.deepCopy(søknadprosess)
    )

    override fun bygg(søknad: Søknad) = MinstEnAvSubsumsjon(
        navn,
        subsumsjoner.map { it.bygg(søknad) }.toMutableList(),
        gyldigSubsumsjon.bygg(søknad),
        ugyldigSubsumsjon.bygg(søknad)
    )

    override fun deepCopy(indeks: Int, søknad: Søknad): Subsumsjon {
        return MinstEnAvSubsumsjon(
            "$navn [$indeks]",
            subsumsjoner.map { it.deepCopy(indeks, søknad) }.toMutableList(),
            gyldigSubsumsjon.deepCopy(indeks, søknad),
            ugyldigSubsumsjon.deepCopy(indeks, søknad)
        )
    }

    override fun deepCopy(): Subsumsjon {
        return MinstEnAvSubsumsjon(
            navn,
            subsumsjoner.map { it.deepCopy() },
            gyldigSubsumsjon.deepCopy(),
            ugyldigSubsumsjon.deepCopy()
        )
    }

    override fun accept(visitor: SubsumsjonVisitor) {
        resultat().also {
            visitor.preVisit(this, it)
            super.accept(visitor)
            visitor.postVisit(this, it)
        }
    }

    override fun lokaltResultat(): Boolean? {
        if (subsumsjoner.any { it.lokaltResultat() == null }) return null
        return subsumsjoner.any { it.lokaltResultat()!! }
    }
}
