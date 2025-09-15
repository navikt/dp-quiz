package no.nav.dagpenger.model.subsumsjon

import no.nav.dagpenger.model.faktum.Faktum
import no.nav.dagpenger.model.faktum.GrunnleggendeFaktum
import no.nav.dagpenger.model.visitor.PrettyPrint
import no.nav.dagpenger.model.visitor.SubsumsjonVisitor

abstract class SammensattSubsumsjon protected constructor(
    navn: String,
    protected open val subsumsjoner: MutableList<Subsumsjon>,
    oppfyltSubsumsjon: Subsumsjon,
    ikkeOppfyltSubsumsjon: Subsumsjon,
) : Subsumsjon(navn, oppfyltSubsumsjon, ikkeOppfyltSubsumsjon),
    MutableList<Subsumsjon> by subsumsjoner {
    override fun alleFakta(): List<Faktum<*>> = subsumsjoner.flatMap { it.alleFakta() }

    override fun nesteFakta(): Set<GrunnleggendeFaktum<*>> =
        subsumsjoner.flatMap { it.nesteFakta() }.toSet().let {
            when (lokaltResultat()) {
                null -> it
                true -> oppfyltSubsumsjon.nesteFakta()
                false -> ikkeOppfyltSubsumsjon.nesteFakta()
            }
        }

    override fun accept(visitor: SubsumsjonVisitor) {
        subsumsjoner.forEach { it.accept(visitor) }
        super.accept(visitor)
    }

    override fun lokaltResultat(): Boolean? {
        if (subsumsjoner.any { it.resultat() == null }) return null
        return subsumsjoner.all { it.resultat()!! }
    }

    override fun toString() = PrettyPrint(this).result()
}
