package no.nav.dagpenger.model.subsumsjon

import no.nav.dagpenger.model.faktum.Faktum
import no.nav.dagpenger.model.faktum.GrunnleggendeFaktum
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.seksjon.Faktagrupper
import no.nav.dagpenger.model.visitor.SubsumsjonVisitor

internal object TomSubsumsjon : Subsumsjon("Tom subsumsjon") {

    override fun deepCopy(faktagrupper: Faktagrupper) = this

    override fun deepCopy(indeks: Int, søknad: Søknad) = this

    override fun bygg(søknad: Søknad) = this

    override fun deepCopy() = this

    override fun accept(visitor: SubsumsjonVisitor) {}

    override fun nesteFakta(): Set<GrunnleggendeFaktum<*>> = emptySet()

    override fun lokaltResultat() = throw IllegalStateException("${this.navn} har ikke resultat")

    override operator fun get(indeks: Int) = throw IllegalArgumentException()

    override fun iterator(): Iterator<Subsumsjon> {
        return object : Iterator<Subsumsjon> {
            override fun hasNext() = false
            override fun next() = throw NoSuchElementException()
        }
    }

    override fun _mulige() = this

    override fun alleFakta(): List<Faktum<*>> = emptyList()
}
