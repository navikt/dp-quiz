package no.nav.dagpenger.model.subsumsjon

import no.nav.dagpenger.model.faktum.GrunnleggendeFaktum
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.seksjon.Søknadprosess
import no.nav.dagpenger.model.visitor.SubsumsjonVisitor

class AlleSubsumsjon private constructor(
    navn: String,
    subsumsjoner: MutableList<Subsumsjon>,
    oppfyltSubsumsjon: Subsumsjon,
    ikkeOppfyltSubsumsjon: Subsumsjon
) : SammensattSubsumsjon(navn, subsumsjoner, oppfyltSubsumsjon, ikkeOppfyltSubsumsjon) {

    internal constructor(navn: String, subsumsjoner: List<Subsumsjon>) : this(
        navn,
        subsumsjoner.toMutableList(),
        TomSubsumsjon,
        TomSubsumsjon
    )

    override fun deepCopy(søknadprosess: Søknadprosess) = AlleSubsumsjon(
        navn,
        subsumsjoner.map { it.deepCopy(søknadprosess) }.toMutableList(),
        oppfyltSubsumsjon.deepCopy(søknadprosess),
        ikkeOppfyltSubsumsjon.deepCopy(søknadprosess)
    )

    override fun bygg(søknad: Søknad) = AlleSubsumsjon(
        navn,
        subsumsjoner.map { it.bygg(søknad) }.toMutableList(),
        oppfyltSubsumsjon.bygg(søknad),
        ikkeOppfyltSubsumsjon.bygg(søknad)
    )

    override fun deepCopy(indeks: Int, søknad: Søknad): Subsumsjon {
        return AlleSubsumsjon(
            "$navn [$indeks]",
            subsumsjoner.map { it.deepCopy(indeks, søknad) }.toMutableList(),
            oppfyltSubsumsjon.deepCopy(indeks, søknad),
            ikkeOppfyltSubsumsjon.deepCopy(indeks, søknad)
        )
    }

    override fun deepCopy(): Subsumsjon {
        return AlleSubsumsjon(
            navn,
            subsumsjoner.map { it.deepCopy() }.toMutableList(),
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
        return subsumsjoner.all { it.resultat()!! }
    }
}
