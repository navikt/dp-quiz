package no.nav.dagpenger.model.søknad

import no.nav.dagpenger.model.fakta.Fakta
import no.nav.dagpenger.model.subsumsjon.Subsumsjon

internal class Versjon(
    private val prototypeFakta: Fakta,
    private val prototypeSubsumsjon: Subsumsjon,
    private val prototypeSøknader: Map<Type, Søknad>
) {

    fun søknad(fnr: String, type: Type): Søknad {
        val fakta = prototypeFakta.bygg(fnr)
        val subsumsjon = prototypeSubsumsjon.bygg(fakta)
        //val søknad = prototypeSøknader[type].bygg(fakta, subsumsjon)
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
