package no.nav.dagpenger.model.faktagrupper

import no.nav.dagpenger.model.faktum.Fakta
import no.nav.dagpenger.model.subsumsjon.Subsumsjon
import java.util.UUID

class Versjon(
    private val versjonId: Int,
    private val prototypeFakta: Fakta,
    private val prototypeSubsumsjon: Subsumsjon,
    private val prototypeFaktaGrupper: Map<FaktagrupperType, Faktagrupper>
) {

    companion object {
        val versjoner = mutableMapOf<Int, Versjon>()
        val siste: Versjon get() = versjoner.maxByOrNull { it.key }?.value ?: throw IllegalArgumentException("Det finnes ingen versjoner!")
        fun id(versjonId: Int) = versjoner[versjonId] ?: throw IllegalArgumentException("Det finnes ingen versjon med id $versjonId")
    }

    fun faktagrupper(fnr: String, type: FaktagrupperType, uuid: UUID = UUID.randomUUID()): Faktagrupper = faktagrupper(prototypeFakta.bygg(fnr, versjonId, uuid), type)

    fun faktagrupper(fakta: Fakta, type: FaktagrupperType): Faktagrupper {
        val subsumsjon = prototypeSubsumsjon.bygg(fakta)
        return prototypeFaktaGrupper[type]?.bygg(fakta, subsumsjon) ?: throw IllegalArgumentException("Kan ikke finne faktagrupper av type $type")
    }

    init {
        versjoner[versjonId] = this
    }

    enum class FaktagrupperType {
        Web,
        Mobile
    }
}
