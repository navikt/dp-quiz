package no.nav.dagpenger.model.seksjon

import no.nav.dagpenger.model.faktum.Fakta
import no.nav.dagpenger.model.faktum.Faktatype
import no.nav.dagpenger.model.faktum.Faktaversjon
import no.nav.dagpenger.model.faktum.Person
import no.nav.dagpenger.model.marshalling.FaktumNavBehov
import no.nav.dagpenger.model.subsumsjon.Subsumsjon
import java.util.UUID

class FaktaVersjonDingseboms private constructor(
    private val bygger: Bygger,
) {
    val faktumNavBehov get() = bygger.faktumNavBehov

    companion object {
        val faktaversjoner = mutableMapOf<Faktaversjon, FaktaVersjonDingseboms>()
        fun siste(faktatype: Faktatype): Faktaversjon {
            return faktaversjoner.keys.filter { it.faktatype.id == faktatype.id }.maxByOrNull { it.versjon }
                ?: throw IllegalArgumentException("Det finnes ingen versjoner av type=$faktatype. Er den registrert?")
        }

        fun id(faktaversjon: Faktaversjon) =
            faktaversjoner[faktaversjon]
                ?: throw IllegalArgumentException("Det finnes ingen versjon med id $faktaversjon")

        fun type(prosesstype: Prosesstype) = faktaversjoner[siste(prosesstype.faktatype)]
            ?: throw IllegalArgumentException("Det finnes ingen prosesstype: $prosesstype")

        fun prosess(
            person: Person,
            prosesstype: Prosesstype,
            prosessUUID: UUID = UUID.randomUUID(),
            faktaUUID: UUID = UUID.randomUUID(),
        ) =
            id(siste(prosesstype.faktatype)).prosess(person, prosesstype, prosessUUID, faktaUUID)
    }

    init {
        require(bygger.faktaversjon() !in faktaversjoner.keys) { "Ugyldig forsøk på å opprette duplikat Versjon ider. id=${bygger.faktaversjon()}" }
        faktaversjoner[bygger.faktaversjon()] = this
    }

    fun fakta(
        person: Person,
        faktaUUID: UUID = UUID.randomUUID(),
    ): Fakta =
        bygger.fakta(person, faktaUUID)

    fun prosess(person: Person, prosesstype: Prosesstype, prosessUUID: UUID, faktaUUID: UUID) =
        bygger.prosess(person, prosesstype, prosessUUID, faktaUUID)

    class ProsessBygger(
        private val faktaBygger: Bygger,
        private val prosess: Prosess,
        private val regeltre: Subsumsjon,
    ) {
        fun prosess(person: Person, prosessUUID: UUID, faktaUUID: UUID): Prosess {
            val fakta = faktaBygger.fakta(person, faktaUUID)
            val subsumsjon = regeltre.bygg(fakta)
            return prosess.bygg(prosessUUID, fakta, subsumsjon)
        }
    }

    class Bygger(
        private val prototypeFakta: Fakta,
        internal val faktumNavBehov: FaktumNavBehov? = null,
        private val prosesser: MutableMap<Prosesstype, ProsessBygger> = mutableMapOf(),
    ) {
        fun fakta(
            person: Person,
            faktaUUID: UUID = UUID.randomUUID(),
        ): Fakta = prototypeFakta.bygg(person, faktaUUID)

        internal fun faktaversjon() = prototypeFakta.faktaversjon
        fun registrer() = FaktaVersjonDingseboms(this)
        fun leggTilProsess(prosess: Prosess, regeltre: Subsumsjon) {
            prosesser[prosess.type] = ProsessBygger(this, prosess, regeltre)
        }

        fun prosess(
            person: Person,
            prosesstype: Prosesstype,
            prosessUUID: UUID = UUID.randomUUID(),
            faktaUUID: UUID = UUID.randomUUID(),
        ) =
            prosesser.filterKeys { it.navn == prosesstype.navn }.values.firstOrNull()
                ?.prosess(person, prosessUUID, faktaUUID)
                ?: throw IllegalArgumentException("Ukjent prosesstype: $prosesstype")
    }
}
