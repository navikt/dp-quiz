package no.nav.dagpenger.model.subsumsjon

import no.nav.dagpenger.model.faktagrupper.Faktagrupper
import no.nav.dagpenger.model.faktum.Fakta
import no.nav.dagpenger.model.faktum.GrunnleggendeFaktum
import no.nav.dagpenger.model.visitor.SubsumsjonVisitor

internal object TomSubsumsjon : Subsumsjon("Tom subsumsjon") {

    override fun deepCopy(faktagrupper: Faktagrupper) = this

    override fun deepCopy(indeks: Int, fakta: Fakta) = this

    override fun bygg(fakta: Fakta) = this

    override fun deepCopy() = this

    override fun accept(visitor: SubsumsjonVisitor) {}

    override fun nesteFakta(): Set<GrunnleggendeFaktum<*>> = emptySet()

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
