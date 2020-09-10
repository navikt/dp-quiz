package no.nav.dagpenger.qamodel.subsumsjon

import no.nav.dagpenger.qamodel.fakta.Faktum
import no.nav.dagpenger.qamodel.regel.Regel
import no.nav.dagpenger.qamodel.visitor.PrettyPrint
import no.nav.dagpenger.qamodel.visitor.SubsumsjonVisitor

class EnkelSubsumsjon internal constructor(
    private val regel: Regel,
    vararg fakta: Faktum<*>
) : Subsumsjon {
    private val fakta = fakta.toSet()
    override val navn = "Enkel subsumsjon"
    override var gyldigSubsumsjon: Subsumsjon = TomSubsumsjon

    override fun konkluder() = regel.konkluder(fakta)

    override fun accept(visitor: SubsumsjonVisitor) {
        visitor.preVisit(this, regel)
        fakta.forEach { it.accept(visitor) }
        acceptGyldig(visitor)
        visitor.postVisit(this, regel)
    }

    override fun fakta(): Set<Faktum<*>> = fakta

    override fun nesteFakta(): Set<Faktum<*>> {
        return mutableSetOf<Faktum<*>>().also {
            fakta.forEach { faktum -> faktum.leggTilHvis(Faktum.FaktumTilstand.Ukjent, it) }
        }
    }

    override fun toString() = PrettyPrint(this).result()
}
