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
        val versjoner = mutableMapOf<Prosesstype, Versjon>()
        val faktamap = mutableMapOf<Faktaversjon, Versjon>()
        fun siste(faktatype: Faktatype): Faktaversjon {
            TODO("Skal nok fases ut")
            /*return versjoner.keys.filter { it.faktatype.id == faktatype.id }.maxByOrNull { it.versjon }
                ?: throw IllegalArgumentException("Det finnes ingen versjoner!")*/
        }

        fun id(prosesstype: Prosesstype) =
            // TODO: Lag en riktig comparsion
            versjoner.values.first() ?: throw IllegalArgumentException("Det finnes ingen versjon med id $prosesstype")

        fun id(faktaversjon: Faktaversjon) =
            faktamap[faktaversjon] ?: throw IllegalArgumentException("Det finnes ingen versjon med id $faktaversjon")
    }

    init {
        require(bygger.prosesstype() !in versjoner.keys) { "Ugyldig forsøk på å opprette duplikat Versjon ider" }
        versjoner[bygger.prosesstype()] = this
        faktamap[bygger.faktaversjon()] = this
    }

    fun fakta(
        person: Person,
        faktaUUID: UUID = UUID.randomUUID(),
    ): Fakta =
        bygger.fakta(person, faktaUUID)

    fun utredningsprosess(fakta: Fakta) =
        bygger.utredningsprosess(fakta)

    fun utredningsprosess(person: Person, prosessUUID: UUID, faktaUUID: UUID) =
        bygger.utredningsprosess(person, prosessUUID, faktaUUID)

    class Bygger(
        private val prototypeFakta: Fakta,
        private val prototypeSubsumsjon: Subsumsjon,
        private val prosess: Prosess,
        internal val faktumNavBehov: FaktumNavBehov? = null,
    ) {
        fun utredningsprosess(
            person: Person,
            prosessUUID: UUID = UUID.randomUUID(),
            faktaUUID: UUID = UUID.randomUUID(),
        ): Prosess =
            utredningsprosess(prototypeFakta.bygg(person, prototypeFakta.faktaversjon, faktaUUID), prosessUUID)

        fun fakta(
            person: Person,
            faktaUUID: UUID = UUID.randomUUID(),
        ): Fakta = prototypeFakta.bygg(person, prototypeFakta.faktaversjon, faktaUUID)

        fun utredningsprosess(
            fakta: Fakta,
            prosessUUID: UUID = UUID.randomUUID(),
        ): Prosess {
            val subsumsjon = prototypeSubsumsjon.bygg(fakta)
            return prosess.bygg(prosessUUID, fakta, subsumsjon)
        }

        internal fun prosesstype() = prosess.type
        internal fun faktaversjon() = prototypeFakta.faktaversjon
        fun registrer() = Versjon(this)
    }
}
