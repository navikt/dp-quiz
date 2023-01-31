package no.nav.dagpenger.model.seksjon

import no.nav.dagpenger.model.faktum.Person
import no.nav.dagpenger.model.faktum.Prosessnavn
import no.nav.dagpenger.model.faktum.Prosessversjon
import no.nav.dagpenger.model.faktum.Fakta
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

    fun søknadprosess(fakta: Fakta, type: UserInterfaceType) =
        bygger.søknadprosess(fakta, type)

    enum class UserInterfaceType(val id: Int) {
        Web(1),
        Mobile(2);

        companion object {
            fun fromId(id: Int): UserInterfaceType = values().first { it.id == id }
        }
    }

    class Bygger(
        private val prototypeFakta: Fakta,
        private val prototypeSubsumsjon: Subsumsjon,
        private val prototypeUserInterfaces: Map<UserInterfaceType, Faktagrupper>,
        internal val faktumNavBehov: FaktumNavBehov? = null
    ) {
        fun søknadprosess(
            person: Person,
            type: UserInterfaceType,
            uuid: UUID = UUID.randomUUID()
        ): Faktagrupper =
            søknadprosess(prototypeFakta.bygg(person, prototypeFakta.prosessVersjon, uuid), type)

        fun søknadprosess(
            fakta: Fakta,
            type: UserInterfaceType
        ): Faktagrupper {
            val subsumsjon = prototypeSubsumsjon.bygg(fakta)
            return prototypeUserInterfaces[type]?.bygg(fakta, subsumsjon)
                ?: throw IllegalArgumentException("Kan ikke finne søknadprosess av type $type")
        }

        internal fun prosessVersjon() = prototypeFakta.prosessVersjon
        fun registrer() = Versjon(this)
    }
}
