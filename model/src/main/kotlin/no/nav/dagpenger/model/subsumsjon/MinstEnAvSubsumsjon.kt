package no.nav.dagpenger.model.subsumsjon

import no.nav.dagpenger.model.faktum.GrunnleggendeFaktum
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.seksjon.Søknadprosess
import no.nav.dagpenger.model.visitor.SubsumsjonVisitor

class MinstEnAvSubsumsjon private constructor(
    navn: String,
    subsumsjoner: List<Subsumsjon>,
    oppfyltSubsumsjon: Subsumsjon,
    ikkeOppfyltSubsumsjon: Subsumsjon
) : SammensattSubsumsjon(navn, subsumsjoner.toMutableList(), oppfyltSubsumsjon, ikkeOppfyltSubsumsjon) {

    internal constructor(navn: String, subsumsjoner: List<Subsumsjon>) : this(
        navn,
        subsumsjoner,
        TomSubsumsjon,
        TomSubsumsjon
    )

    override fun deepCopy(søknadprosess: Søknadprosess) = MinstEnAvSubsumsjon(
        navn,
        subsumsjoner.map { it.deepCopy(søknadprosess) },
        oppfyltSubsumsjon.deepCopy(søknadprosess),
        ikkeOppfyltSubsumsjon.deepCopy(søknadprosess)
    )

    override fun bygg(søknad: Søknad) = MinstEnAvSubsumsjon(
        navn,
        subsumsjoner.map { it.bygg(søknad) }.toMutableList(),
        oppfyltSubsumsjon.bygg(søknad),
        ikkeOppfyltSubsumsjon.bygg(søknad)
    )

    override fun deepCopy(indeks: Int, søknad: Søknad): Subsumsjon {
        return MinstEnAvSubsumsjon(
            "$navn [$indeks]",
            subsumsjoner.map { it.deepCopy(indeks, søknad) }.toMutableList(),
            oppfyltSubsumsjon.deepCopy(indeks, søknad),
            ikkeOppfyltSubsumsjon.deepCopy(indeks, søknad)
        )
    }

    override fun deepCopy(): Subsumsjon {
        return MinstEnAvSubsumsjon(
            navn,
            subsumsjoner.map { it.deepCopy() },
            oppfyltSubsumsjon.deepCopy(),
            ikkeOppfyltSubsumsjon.deepCopy()
        )
    }

    override fun nesteFakta(): Set<GrunnleggendeFaktum<*>> =
        subsumsjoner.map { it.nesteFakta() }.filterNot { it.isEmpty() }.let {
            when (lokaltResultat()) {
                null -> it.firstOrNull() ?: emptySet()
                true -> oppfyltSubsumsjon.nesteFakta()
                false -> ikkeOppfyltSubsumsjon.nesteFakta()
            }
        }

    override fun accept(visitor: SubsumsjonVisitor) {
        resultat().also {
            visitor.preVisit(this, subsumsjoner, lokaltResultat(), it)
            super.accept(visitor)
            visitor.postVisit(this, subsumsjoner, lokaltResultat(), it)
        }
    }

    override fun lokaltResultat(): Boolean? {
        if (subsumsjoner.any { it.resultat() == null }) return null
        return subsumsjoner.any { it.resultat()!! }
    }
}
