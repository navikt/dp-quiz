package no.nav.dagpenger.model.subsumsjon

import no.nav.dagpenger.model.fakta.Faktum
import no.nav.dagpenger.model.fakta.FaktumNavn
import no.nav.dagpenger.model.fakta.GrunnleggendeFaktum
import no.nav.dagpenger.model.visitor.SubsumsjonVisitor

internal object TomSubsumsjon : Subsumsjon("Tom subsumsjon") {

    override fun deepCopy(faktaMap: Map<FaktumNavn, Faktum<*>>) = this

    override fun deepCopy() = this

    override fun accept(visitor: SubsumsjonVisitor) {}

    override fun nesteFakta(): Set<GrunnleggendeFaktum<*>> = emptySet()

    override fun enkelSubsumsjoner(vararg fakta: Faktum<*>) = emptyList<EnkelSubsumsjon>()

    override fun lokaltResultat() = throw IllegalStateException()

    override operator fun get(indeks: Int) = throw IllegalArgumentException()

    override fun iterator(): Iterator<Subsumsjon> {
        return object : Iterator<Subsumsjon> {
            override fun hasNext() = false
            override fun next() = throw NoSuchElementException()
        }
    }

    override fun _mulige() = this
}
