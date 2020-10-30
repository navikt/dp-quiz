package no.nav.dagpenger.model.subsumsjon

import no.nav.dagpenger.model.faktum.GrunnleggendeFaktum
import no.nav.dagpenger.model.visitor.PrettyPrint
import no.nav.dagpenger.model.visitor.SubsumsjonVisitor

abstract class SammensattSubsumsjon protected constructor(
    navn: String,
    protected open val subsumsjoner: MutableList<Subsumsjon>,
    gyldigSubsumsjon: Subsumsjon,
    ugyldigSubsumsjon: Subsumsjon
) : Subsumsjon(navn, gyldigSubsumsjon, ugyldigSubsumsjon), MutableList<Subsumsjon> by subsumsjoner {

    internal constructor(navn: String, subsumsjoner: List<Subsumsjon>) :
        this(navn, subsumsjoner.toMutableList(), TomSubsumsjon, TomSubsumsjon)

    override fun nesteFakta(): Set<GrunnleggendeFaktum<*>> =
        subsumsjoner.flatMap { it.nesteFakta() }.toSet().let {
            when (lokaltResultat()) {
                null -> it
                true -> gyldigSubsumsjon.nesteFakta()
                false -> ugyldigSubsumsjon.nesteFakta()
            }
        }
    override fun accept(visitor: SubsumsjonVisitor) {
        subsumsjoner.forEach { it.accept(visitor) }
        super.accept(visitor)
    }

    override fun lokaltResultat(): Boolean? {
        if (subsumsjoner.any { it.lokaltResultat() == null }) return null
        return subsumsjoner.all { it.lokaltResultat()!! }
    }

    override fun toString() = PrettyPrint(this).result()
}
