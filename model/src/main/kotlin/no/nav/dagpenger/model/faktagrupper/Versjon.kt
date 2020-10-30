package no.nav.dagpenger.model.faktagrupper

import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.subsumsjon.Subsumsjon
import java.util.UUID

class Versjon(
        private val versjonId: Int,
        private val prototypeSøknad: Søknad,
        private val prototypeSubsumsjon: Subsumsjon,
        private val prototypeFaktaGrupper: Map<FaktagrupperType, Faktagrupper>
) {

    companion object {
        val versjoner = mutableMapOf<Int, Versjon>()
        val siste: Versjon get() = versjoner.maxByOrNull { it.key }?.value ?: throw IllegalArgumentException("Det finnes ingen versjoner!")
        fun id(versjonId: Int) = versjoner[versjonId] ?: throw IllegalArgumentException("Det finnes ingen versjon med id $versjonId")
    }

    fun faktagrupper(fnr: String, type: FaktagrupperType, uuid: UUID = UUID.randomUUID()): Faktagrupper = faktagrupper(prototypeSøknad.bygg(fnr, versjonId, uuid), type)

    fun faktagrupper(søknad: Søknad, type: FaktagrupperType): Faktagrupper {
        val subsumsjon = prototypeSubsumsjon.bygg(søknad)
        return prototypeFaktaGrupper[type]?.bygg(søknad, subsumsjon) ?: throw IllegalArgumentException("Kan ikke finne faktagrupper av type $type")
    }

    init {
        versjoner[versjonId] = this
    }

    enum class FaktagrupperType {
        Web,
        Mobile
    }
}
