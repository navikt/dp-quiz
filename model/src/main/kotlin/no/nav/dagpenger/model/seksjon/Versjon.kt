package no.nav.dagpenger.model.seksjon

import no.nav.dagpenger.model.faktum.Person
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.marshalling.FaktumNavBehov
import no.nav.dagpenger.model.subsumsjon.Subsumsjon
import java.util.UUID

class Versjon private constructor(
    private val bygger: Bygger
) {
    val faktumNavBehov get() = bygger.faktumNavBehov

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

    fun søknadprosess(
        person: Person,
        type: UserInterfaceType,
        dryRun: Boolean = true,
        uuid: UUID = UUID.randomUUID()
    ): Søknadprosess =
        bygger.søknadprosess(person, type, uuid, dryRun)

    fun søknadprosess(søknad: Søknad, type: UserInterfaceType, dryRun: Boolean = true) =
        bygger.søknadprosess(søknad, type, dryRun)

    enum class UserInterfaceType(val id: Int) {
        Web(1),
        Mobile(2);

        companion object {
            fun fromId(id: Int): UserInterfaceType = values().first { it.id == id }
        }
    }

    class Bygger(
        private val prototypeSøknad: Søknad,
        private val prototypeSubsumsjon: Subsumsjon,
        private val prototypeUserInterfaces: Map<UserInterfaceType, Søknadprosess>,
        internal val faktumNavBehov: FaktumNavBehov? = null
    ) {
        fun søknadprosess(
            person: Person,
            type: UserInterfaceType,
            uuid: UUID = UUID.randomUUID(),
            dryRun: Boolean = true
        ): Søknadprosess =
            søknadprosess(prototypeSøknad.bygg(person, prototypeSøknad.versjonId, uuid), type, dryRun)

        fun søknadprosess(
            søknad: Søknad,
            type: UserInterfaceType,
            dryRun: Boolean = true
        ): Søknadprosess {
            val subsumsjon = prototypeSubsumsjon.bygg(søknad)
            return prototypeUserInterfaces[type]?.bygg(søknad, subsumsjon, dryRun)
                ?: throw IllegalArgumentException("Kan ikke finne søknadprosess av type $type")
        }

        internal fun versjonId() = prototypeSøknad.versjonId
        fun registrer() = Versjon(this)
    }
}
