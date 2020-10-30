package no.nav.dagpenger.model.subsumsjon

import no.nav.dagpenger.model.fakta.Fakta
import no.nav.dagpenger.model.faktagrupper.Faktagrupper
import no.nav.dagpenger.model.visitor.SubsumsjonVisitor

class MinstEnAvSubsumsjon private constructor(
    navn: String,
    subsumsjoner: List<Subsumsjon>,
    gyldigSubsumsjon: Subsumsjon,
    ugyldigSubsumsjon: Subsumsjon
) : SammensattSubsumsjon(navn, subsumsjoner.toMutableList(), gyldigSubsumsjon, ugyldigSubsumsjon) {

    internal constructor(navn: String, subsumsjoner: List<Subsumsjon>) : this(navn, subsumsjoner, TomSubsumsjon, TomSubsumsjon)

    override fun deepCopy(faktagrupper: Faktagrupper) = MinstEnAvSubsumsjon(
        navn,
        subsumsjoner.map { it.deepCopy(faktagrupper) },
        gyldigSubsumsjon.deepCopy(faktagrupper),
        ugyldigSubsumsjon.deepCopy(faktagrupper)
    )

    override fun bygg(fakta: Fakta) = MinstEnAvSubsumsjon(
        navn,
        subsumsjoner.map { it.bygg(fakta) }.toMutableList(),
        gyldigSubsumsjon.bygg(fakta),
        ugyldigSubsumsjon.bygg(fakta)
    )

    override fun deepCopy(indeks: Int, fakta: Fakta): Subsumsjon {
        return MinstEnAvSubsumsjon(
            "$navn [$indeks]",
            subsumsjoner.map { it.deepCopy(indeks, fakta) }.toMutableList(),
            gyldigSubsumsjon.deepCopy(indeks, fakta),
            ugyldigSubsumsjon.deepCopy(indeks, fakta)
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
}
