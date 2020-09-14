package no.nav.dagpenger.model.subsumsjon

import no.nav.dagpenger.model.fakta.Faktum
import no.nav.dagpenger.model.visitor.PrettyPrint
import no.nav.dagpenger.model.visitor.SubsumsjonVisitor

class AlleSubsumsjon internal constructor(
    navn: String,
    private val subsumsjoner: List<Subsumsjon>
) : Subsumsjon(navn) {

    override fun konkluder() = subsumsjoner.all { it.konkluder() }

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

    override fun _sti(subsumsjon: Subsumsjon): List<Subsumsjon> {
        if (this == subsumsjon) return listOf(this)

        (subsumsjoner + listOf(gyldig, ugyldig)).forEach {
            it._sti(subsumsjon).also { child ->
                if (child.isNotEmpty()) return listOf(this) + child
            }
        }
        return emptyList()
    }

    override fun _resultat(): Boolean? {
        if (subsumsjoner.any { it._resultat() == null }) return null
        return subsumsjoner.all { it._resultat()!! }
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
