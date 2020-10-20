package no.nav.dagpenger.model.søknad

import no.nav.dagpenger.model.fakta.Fakta
import no.nav.dagpenger.model.subsumsjon.Subsumsjon

internal class Versjon(private val prototypeFakta: Fakta, private val prototypeSubsumsjon: Subsumsjon, prototypeSøknader: Map<Type, Søknad>) {

    fun søknad(fnr: String, type: Type): Søknad {
        val fakta = prototypeFakta.bygg(fnr)
        return Søknad(fakta, prototypeSubsumsjon)
    }

    companion object {
        val versjoner = mutableListOf<Versjon>()
        val siste: Versjon get() = versjoner.last()
    }

    init {
        versjoner.add(this)
    }

    enum class Type {
        Web,
        Mobile
    }
}
