package no.nav.dagpenger.qamodel.subsumsjon

import no.nav.dagpenger.qamodel.fakta.Faktum
import no.nav.dagpenger.qamodel.regel.Regel
import no.nav.dagpenger.qamodel.visitor.PrettyPrint
import no.nav.dagpenger.qamodel.visitor.SubsumsjonVisitor

class EnkelSubsumsjon internal constructor(
    private val regel: Regel,
    vararg fakta: Faktum<*>
) : Subsumsjon("Enkel subsumsjon") {
    private val fakta = fakta.toSet()

    override fun konkluder() = regel.konkluder(fakta)

    override fun accept(visitor: SubsumsjonVisitor) {
        visitor.preVisit(this, regel)
        fakta.forEach { it.accept(visitor) }
        acceptGyldig(visitor)
        acceptUgyldig(visitor)
        visitor.postVisit(this, regel)
    }

    override fun fakta(): Set<Faktum<*>> = fakta

    override fun nesteFakta(): Set<Faktum<*>> {
        return mutableSetOf<Faktum<*>>().also {
            fakta.forEach { faktum -> faktum.leggTilHvis(Faktum.FaktumTilstand.Ukjent, it) }
            if (it.isNotEmpty()) return it
            (if (konkluder()) gyldigSubsumsjon else ugyldigSubsumsjon).nesteFakta()
        }
    }

    override fun toString() = PrettyPrint(this).result()

    internal operator fun get(indeks: Int): Subsumsjon = throw IllegalArgumentException()
}
