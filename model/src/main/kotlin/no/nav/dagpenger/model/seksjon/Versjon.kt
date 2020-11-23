package no.nav.dagpenger.model.seksjon

import no.nav.dagpenger.model.faktum.Person
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.subsumsjon.Subsumsjon
import java.util.UUID

class Versjon(
    private val prototypeSøknad: Søknad,
    private val prototypeSubsumsjon: Subsumsjon,
    private val prototypeUserInterfaces: Map<UserInterfaceType, Søknadprosess>
) {

    companion object {
        val versjoner = mutableMapOf<Int, Versjon>()
        val siste: Int
            get() = versjoner.keys.maxOrNull()
                ?: throw IllegalArgumentException("Det finnes ingen versjoner!")

        fun id(versjonId: Int) =
            versjoner[versjonId] ?: throw IllegalArgumentException("Det finnes ingen versjon med id $versjonId")
    }

    init {
        require(prototypeSøknad.versjonId !in versjoner.keys) { "Ugyldig forsøk på å opprette duplikat Versjon ider" }
        versjoner[prototypeSøknad.versjonId] = this
    }

    fun søknadprosess(person: Person, type: UserInterfaceType, uuid: UUID = UUID.randomUUID()): Søknadprosess =
        søknadprosess(prototypeSøknad.bygg(person.fnr, prototypeSøknad.versjonId, uuid), type)

    fun søknadprosess(fnr: String, type: UserInterfaceType, uuid: UUID = UUID.randomUUID()): Søknadprosess =
        søknadprosess(prototypeSøknad.bygg(fnr, prototypeSøknad.versjonId, uuid), type)

    fun søknadprosess(søknad: Søknad, type: UserInterfaceType): Søknadprosess {
        val subsumsjon = prototypeSubsumsjon.bygg(søknad)
        return prototypeUserInterfaces[type]?.bygg(søknad, subsumsjon)
            ?: throw IllegalArgumentException("Kan ikke finne søknadprosess av type $type")
    }

    enum class UserInterfaceType(val id: Int) {
        Web(1),
        Mobile(2);

        companion object {
            fun fromId(id: Int): UserInterfaceType = values().first { it.id == id }
        }
    }
}
