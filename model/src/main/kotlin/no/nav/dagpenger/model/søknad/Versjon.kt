package no.nav.dagpenger.model.søknad

import no.nav.dagpenger.model.fakta.Fakta
import no.nav.dagpenger.model.subsumsjon.Subsumsjon

class Versjon(
    private val versjonId: Int,
    private val prototypeFakta: Fakta,
    private val prototypeSubsumsjon: Subsumsjon,
    private val prototypeSøknader: Map<Type, Søknad>
) {

    fun søknad(fnr: String, type: Type): Søknad {
        val fakta = prototypeFakta.bygg(fnr, versjonId)
        val subsumsjon = prototypeSubsumsjon.bygg(fakta)
        return prototypeSøknader[type]?.bygg(fakta, subsumsjon) ?: throw IllegalArgumentException("Kan ikke finne søknad av type $type")
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
