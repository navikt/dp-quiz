package no.nav.dagpenger.qamodel.subsumsjon

import no.nav.dagpenger.qamodel.fakta.Faktum

internal object TomSubsumsjon : Subsumsjon {
    override var gyldigSubsumsjon: Subsumsjon = this
    override val navn = "Tom subsumsjon"

    override fun konkluder() = true

    override fun fakta(): Set<Faktum<*>> = emptySet()

    override fun nesteFakta(): Set<Faktum<*>> = emptySet()
}
