package no.nav.dagpenger.model.søknad

import no.nav.dagpenger.model.fakta.Fakta
import no.nav.dagpenger.model.subsumsjon.Subsumsjon
import java.util.UUID

class Versjon(
    private val versjonId: Int,
    private val prototypeFakta: Fakta,
    private val prototypeSubsumsjon: Subsumsjon,
    private val prototypeSøknader: Map<Type, Faktagrupper>
) {

    companion object {
        val versjoner = mutableMapOf<Int, Versjon>()
        val siste: Versjon get() = versjoner.maxByOrNull { it.key }?.value ?: throw IllegalArgumentException("Det finnes ingen versjoner!")
        fun id(versjonId: Int) = versjoner[versjonId] ?: throw IllegalArgumentException("Det finnes ingen versjon med id $versjonId")
    }

    fun søknad(fnr: String, type: Type, uuid: UUID = UUID.randomUUID()): Faktagrupper = søknad(prototypeFakta.bygg(fnr, versjonId, uuid), type)

    fun søknad(fakta: Fakta, type: Type): Faktagrupper {
        val subsumsjon = prototypeSubsumsjon.bygg(fakta)
        return prototypeSøknader[type]?.bygg(fakta, subsumsjon) ?: throw IllegalArgumentException("Kan ikke finne søknad av type $type")
    }

    init {
        versjoner[versjonId] = this
    }

    enum class Type {
        Web,
        Mobile
    }
}
