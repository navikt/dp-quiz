package no.nav.dagpenger.model.seksjon

import no.nav.dagpenger.model.faktum.Person
import no.nav.dagpenger.model.faktum.Prosessnavn
import no.nav.dagpenger.model.faktum.Prosessversjon
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.marshalling.FaktumNavBehov
import no.nav.dagpenger.model.subsumsjon.Subsumsjon
import java.util.UUID

class Versjon private constructor(
    private val bygger: Bygger
) {
    val faktumNavBehov get() = bygger.faktumNavBehov

    companion object {
        val versjoner = mutableMapOf<Prosessversjon, Versjon>()
        fun siste(prosessnavn: Prosessnavn): Prosessversjon {
            return versjoner.keys.filter { it.prosessnavn.id == prosessnavn.id }.maxByOrNull { it.versjon }
                ?: throw IllegalArgumentException("Det finnes ingen versjoner!")
        }

        fun id(versjonId: Prosessversjon) =
            versjoner[versjonId] ?: throw IllegalArgumentException("Det finnes ingen versjon med id $versjonId")
    }

    init {
        require(bygger.prosessVersjon() !in versjoner.keys) { "Ugyldig forsøk på å opprette duplikat Versjon ider" }
        versjoner[bygger.prosessVersjon()] = this
    }

    fun søknadprosess(
        person: Person,
        type: UserInterfaceType,
        uuid: UUID = UUID.randomUUID()
    ): Faktagrupper =
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

    class Bygger(
        private val prototypeSøknad: Søknad,
        private val prototypeSubsumsjon: Subsumsjon,
        private val prototypeUserInterfaces: Map<UserInterfaceType, Faktagrupper>,
        internal val faktumNavBehov: FaktumNavBehov? = null
    ) {
        fun søknadprosess(
            person: Person,
            type: UserInterfaceType,
            uuid: UUID = UUID.randomUUID()
        ): Faktagrupper =
            søknadprosess(prototypeSøknad.bygg(person, prototypeSøknad.prosessVersjon, uuid), type)

        fun søknadprosess(
            søknad: Søknad,
            type: UserInterfaceType
        ): Faktagrupper {
            val subsumsjon = prototypeSubsumsjon.bygg(søknad)
            return prototypeUserInterfaces[type]?.bygg(søknad, subsumsjon)
                ?: throw IllegalArgumentException("Kan ikke finne søknadprosess av type $type")
        }

        internal fun prosessVersjon() = prototypeSøknad.prosessVersjon
        fun registrer() = Versjon(this)
    }
}
