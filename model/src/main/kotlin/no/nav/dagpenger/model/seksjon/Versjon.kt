package no.nav.dagpenger.model.seksjon

import no.nav.dagpenger.model.faktum.Fakta
import no.nav.dagpenger.model.faktum.Faktatype
import no.nav.dagpenger.model.faktum.Faktaversjon
import no.nav.dagpenger.model.faktum.Person
import no.nav.dagpenger.model.marshalling.FaktumNavBehov
import no.nav.dagpenger.model.subsumsjon.Subsumsjon
import java.util.UUID

class Versjon private constructor(
    private val bygger: Bygger,
) {
    val faktumNavBehov get() = bygger.faktumNavBehov

    companion object {
        val versjoner = mutableMapOf<Faktaversjon, Versjon>()
        fun siste(faktatype: Faktatype): Faktaversjon {
            return versjoner.keys.filter { it.faktatype.id == faktatype.id }.maxByOrNull { it.versjon }
                ?: throw IllegalArgumentException("Det finnes ingen versjoner!")
        }

        fun id(versjonId: Faktaversjon) =
            versjoner[versjonId] ?: throw IllegalArgumentException("Det finnes ingen versjon med id $versjonId")
    }

    init {
        require(bygger.prosessversjon() !in versjoner.keys) { "Ugyldig forsøk på å opprette duplikat Versjon ider" }
        versjoner[bygger.prosessversjon()] = this
    }

    fun fakta(
        person: Person,
        faktaUUID: UUID = UUID.randomUUID(),
    ): Fakta =
        bygger.fakta(person, faktaUUID)

    fun utredningsprosess(fakta: Fakta) =
        bygger.utredningsprosess(fakta)

    fun utredningsprosess(person: Person, faktaUUID: UUID) =
        bygger.utredningsprosess(person, faktaUUID)

    class Bygger(
        private val prototypeFakta: Fakta,
        private val prototypeSubsumsjon: Subsumsjon,
        private val utredningsprosess: Utredningsprosess,
        internal val faktumNavBehov: FaktumNavBehov? = null,
    ) {
        fun utredningsprosess(
            person: Person,
            faktaUUID: UUID = UUID.randomUUID(),
        ): Utredningsprosess =
            utredningsprosess(prototypeFakta.bygg(person, prototypeFakta.faktaversjon, faktaUUID))

        fun fakta(
            person: Person,
            faktaUUID: UUID = UUID.randomUUID(),
        ): Fakta = prototypeFakta.bygg(person, prototypeFakta.faktaversjon, faktaUUID)

        fun utredningsprosess(
            fakta: Fakta,
        ): Utredningsprosess {
            val subsumsjon = prototypeSubsumsjon.bygg(fakta)
            return utredningsprosess.bygg(fakta, subsumsjon)
        }

        internal fun prosessversjon() = prototypeFakta.faktaversjon
        fun registrer() = Versjon(this)
    }
}
