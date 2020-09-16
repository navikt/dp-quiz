package no.nav.dagpenger.model.subsumsjon

import no.nav.dagpenger.model.fakta.Faktum
import no.nav.dagpenger.model.fakta.GrunnleggendeFaktum
import no.nav.dagpenger.model.visitor.PrettyPrint
import no.nav.dagpenger.model.visitor.SubsumsjonVisitor

abstract class SammensattSubsumsjon(
    navn: String,
    protected open val subsumsjoner: List<Subsumsjon>
) : Subsumsjon(navn) {

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

    override fun _sti(subsumsjon: Subsumsjon): List<Subsumsjon> {
        if (this == subsumsjon) return listOf(this)

        (subsumsjoner + listOf(gyldig, ugyldig)).forEach {
            it._sti(subsumsjon).also { child ->
                if (child.isNotEmpty()) return listOf(this) + child
            }
        }
        return emptyList()
    }

    override fun lokaltResultat(): Boolean? {
        if (subsumsjoner.any { it.lokaltResultat() == null }) return null
        return subsumsjoner.all { it.lokaltResultat()!! }
    }

    override fun enkelSubsumsjoner(vararg fakta: Faktum<*>): List<EnkelSubsumsjon> =
        subsumsjoner.flatMap { it.enkelSubsumsjoner(*fakta) } +
            gyldigSubsumsjon.enkelSubsumsjoner(*fakta) +
            ugyldigSubsumsjon.enkelSubsumsjoner(*fakta)

    override fun fakta() =
        subsumsjoner.flatMap { it.fakta() }.toSet() + gyldigSubsumsjon.fakta() + ugyldigSubsumsjon.fakta()

    override fun toString() = PrettyPrint(this).result()
    override operator fun get(indeks: Int) = subsumsjoner[indeks]
    override fun iterator(): Iterator<Subsumsjon> {
        val iterator = subsumsjoner.iterator()
        return object : Iterator<Subsumsjon> {
            override fun hasNext() = iterator.hasNext()
            override fun next() = iterator.next()
        }
    }
}
