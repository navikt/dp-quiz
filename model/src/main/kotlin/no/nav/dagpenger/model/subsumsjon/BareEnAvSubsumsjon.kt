package no.nav.dagpenger.model.subsumsjon

import no.nav.dagpenger.model.faktum.Fakta
import no.nav.dagpenger.model.seksjon.Prosess
import no.nav.dagpenger.model.visitor.SubsumsjonVisitor

class BareEnAvSubsumsjon private constructor(
    navn: String,
    subsumsjoner: List<Subsumsjon>,
    oppfyltSubsumsjon: Subsumsjon,
    ikkeOppfyltSubsumsjon: Subsumsjon,
) : SammensattSubsumsjon(navn, subsumsjoner.toMutableList(), oppfyltSubsumsjon, ikkeOppfyltSubsumsjon) {
    internal constructor(navn: String, subsumsjoner: List<Subsumsjon>) : this(
        navn,
        subsumsjoner,
        TomSubsumsjon,
        TomSubsumsjon,
    )

    override fun deepCopy(prosess: Prosess) =
        BareEnAvSubsumsjon(
            navn,
            subsumsjoner.map { it.deepCopy(prosess) },
            oppfyltSubsumsjon.deepCopy(prosess),
            ikkeOppfyltSubsumsjon.deepCopy(prosess),
        )

    override fun bygg(fakta: Fakta) =
        BareEnAvSubsumsjon(
            navn,
            subsumsjoner.map { it.bygg(fakta) }.toMutableList(),
            oppfyltSubsumsjon.bygg(fakta),
            ikkeOppfyltSubsumsjon.bygg(fakta),
        )

    override fun deepCopy(
        indeks: Int,
        fakta: Fakta,
    ): Subsumsjon =
        BareEnAvSubsumsjon(
            "$navn [$indeks]",
            subsumsjoner.map { it.deepCopy(indeks, fakta) }.toMutableList(),
            oppfyltSubsumsjon.deepCopy(indeks, fakta),
            ikkeOppfyltSubsumsjon.deepCopy(indeks, fakta),
        )

    override fun deepCopy(): Subsumsjon =
        BareEnAvSubsumsjon(
            navn,
            subsumsjoner.map { it.deepCopy() },
            oppfyltSubsumsjon.deepCopy(),
            ikkeOppfyltSubsumsjon.deepCopy(),
        )

    override fun accept(visitor: SubsumsjonVisitor) {
        resultat().also {
            visitor.preVisit(this, lokaltResultat(), it)
            super.accept(visitor)
            visitor.postVisit(this, lokaltResultat(), it)
        }
    }

    override fun lokaltResultat(): Boolean? {
        if (subsumsjoner.any { it.resultat() == null }) return null
        return subsumsjoner.filter { it.resultat()!! }.size == 1
    }
}
