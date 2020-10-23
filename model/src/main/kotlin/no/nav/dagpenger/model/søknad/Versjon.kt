package no.nav.dagpenger.model.søknad

import no.nav.dagpenger.model.fakta.Fakta
import no.nav.dagpenger.model.subsumsjon.Subsumsjon

class Versjon(
    private val versjonId: Int,
    private val prototypeFakta: Fakta,
    private val prototypeSubsumsjon: Subsumsjon,
    private val prototypeSøknader: Map<Type, Søknad>
) {

    fun søknad(fnr: String, type: Type): Søknad = søknad(prototypeFakta.bygg(fnr, versjonId), type)

    fun søknad(fakta: Fakta, type: Type): Søknad {
        val subsumsjon = prototypeSubsumsjon.bygg(fakta)
        return prototypeSøknader[type]?.bygg(fakta, subsumsjon) ?: throw IllegalArgumentException("Kan ikke finne søknad av type $type")
    }

    companion object {
        val versjoner = mutableMapOf<Int, Versjon>()
        val siste: Versjon get() = versjoner.maxByOrNull { it.key }?.value ?: throw IllegalArgumentException("Det finnes ingen versjoner!")
        fun id(versjonId: Int) = versjoner[versjonId] ?: throw IllegalArgumentException("Det finnes ingen versjon med id $versjonId")
    }

    init {
        versjoner[versjonId] = this
    }

    enum class Type {
        Web,
        Mobile
    }
}
