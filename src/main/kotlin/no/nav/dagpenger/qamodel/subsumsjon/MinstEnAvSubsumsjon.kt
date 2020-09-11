package no.nav.dagpenger.qamodel.subsumsjon

import no.nav.dagpenger.qamodel.fakta.Faktum
import no.nav.dagpenger.qamodel.visitor.PrettyPrint
import no.nav.dagpenger.qamodel.visitor.SubsumsjonVisitor

class MinstEnAvSubsumsjon internal constructor(
    navn: String,
    private val subsumsjoner: List<Subsumsjon>
) : Subsumsjon(navn) {
    override fun konkluder() = subsumsjoner.any { it.konkluder() }

    override fun accept(visitor: SubsumsjonVisitor) {
        visitor.preVisit(this)
        subsumsjoner.forEach { it.accept(visitor) }
        acceptGyldig(visitor)
        acceptUgyldig(visitor)
        visitor.postVisit(this)
    }

    override fun nesteFakta(): Set<Faktum<*>> =
        subsumsjoner.flatMap { it.nesteFakta() }.toSet().let {
            if (it.isNotEmpty()) return it
            (if (konkluder()) gyldigSubsumsjon else ugyldigSubsumsjon).nesteFakta()
        }

    override fun subsumsjoner(vararg fakta: Faktum<*>): List<EnkelSubsumsjon> =
        subsumsjoner.flatMap { it.subsumsjoner(*fakta) }

    override fun fakta() = subsumsjoner.flatMap { it.fakta() }.toSet() + gyldigSubsumsjon.fakta()

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
