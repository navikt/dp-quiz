package no.nav.dagpenger.model.seksjon

import no.nav.dagpenger.model.faktum.Person
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.marshalling.FaktumNavBehov
import no.nav.dagpenger.model.subsumsjon.Subsumsjon
import java.util.UUID

class Versjon(
    private val bygger: VersjonBygger
) {
    val faktumNavBehov get() = bygger.faktumNavBehov

    constructor(
        prototypeSøknad: Søknad,
        prototypeSubsumsjon: Subsumsjon,
        prototypeUserInterfaces: Map<UserInterfaceType, Søknadprosess>,
        faktumNavBehov: FaktumNavBehov? = null
    ) : this(VersjonBygger(prototypeSøknad, prototypeSubsumsjon, prototypeUserInterfaces, faktumNavBehov))

    companion object {
        val versjoner = mutableMapOf<Int, Versjon>()
        val siste: Int
            get() = versjoner.keys.maxOrNull()
                ?: throw IllegalArgumentException("Det finnes ingen versjoner!")

        fun id(versjonId: Int) =
            versjoner[versjonId] ?: throw IllegalArgumentException("Det finnes ingen versjon med id $versjonId")
    }

    init {
        require(bygger.versjonId() !in versjoner.keys) { "Ugyldig forsøk på å opprette duplikat Versjon ider" }
        versjoner[bygger.versjonId()] = this
    }

    fun søknadprosess(person: Person, type: UserInterfaceType, uuid: UUID = UUID.randomUUID()): Søknadprosess =
        bygger.søknadprosess(person, type, uuid)

    fun søknadprosess(søknad: Søknad, type: UserInterfaceType) =
        bygger.søknadprosess(søknad, type)

    enum class UserInterfaceType(val id: Int) {
        Web(1),
        Mobile(2);

        companion object {
            fun fromId(id: Int): UserInterfaceType = values().first { it.id == id }
        }
    }

    class VersjonBygger(
        private val prototypeSøknad: Søknad,
        private val prototypeSubsumsjon: Subsumsjon,
        private val prototypeUserInterfaces: Map<Versjon.UserInterfaceType, Søknadprosess>,
        internal val faktumNavBehov: FaktumNavBehov? = null
    ) {
        fun søknadprosess(
            person: Person,
            type: Versjon.UserInterfaceType,
            uuid: UUID = UUID.randomUUID()
        ): Søknadprosess =
            søknadprosess(prototypeSøknad.bygg(person, prototypeSøknad.versjonId, uuid), type)

        fun søknadprosess(søknad: Søknad, type: Versjon.UserInterfaceType): Søknadprosess {
            val subsumsjon = prototypeSubsumsjon.bygg(søknad)
            return prototypeUserInterfaces[type]?.bygg(søknad, subsumsjon)
                ?: throw IllegalArgumentException("Kan ikke finne søknadprosess av type $type")
        }

        internal fun versjonId() = prototypeSøknad.versjonId
    }
}
