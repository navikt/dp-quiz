package no.nav.dagpenger.model.seksjon

import no.nav.dagpenger.model.faktum.Fakta
import no.nav.dagpenger.model.faktum.Faktatype
import no.nav.dagpenger.model.faktum.Faktaversjon
import no.nav.dagpenger.model.faktum.Person
import no.nav.dagpenger.model.marshalling.FaktumNavBehov
import no.nav.dagpenger.model.subsumsjon.Subsumsjon
import java.util.UUID

class Henvendelser private constructor(
    private val faktaBygger: FaktaBygger,
) {
    val faktumNavBehov get() = faktaBygger.faktumNavBehov

    companion object {
        val henvendelser = mutableMapOf<Faktaversjon, Henvendelser>()

        fun siste(faktatype: Faktatype): Faktaversjon =
            henvendelser.keys.filter { it.faktatype.id == faktatype.id }.maxByOrNull { it.versjon }
                ?: throw IllegalArgumentException("Det finnes ingen versjoner av type=$faktatype. Er den registrert?")

        fun id(faktaversjon: Faktaversjon) =
            henvendelser[faktaversjon]
                ?: throw IllegalArgumentException("Det finnes ingen versjon med id $faktaversjon")

        fun type(prosesstype: Prosesstype) =
            henvendelser[siste(prosesstype.faktatype)]
                ?: throw IllegalArgumentException("Det finnes ingen prosesstype: $prosesstype")

        fun prosess(
            person: Person,
            prosesstype: Prosesstype,
            prosessUUID: UUID = UUID.randomUUID(),
            faktaUUID: UUID = UUID.randomUUID(),
        ) = id(siste(prosesstype.faktatype)).prosess(person, prosesstype, prosessUUID, faktaUUID)

        fun prosess(
            person: Person,
            prosesstype: Prosesstype,
            prosessUUID: UUID = UUID.randomUUID(),
            faktaUUID: UUID = UUID.randomUUID(),
            faktaversjon: Faktaversjon,
        ) = id(faktaversjon).prosess(person, prosesstype, prosessUUID, faktaUUID)
    }

    init {
        require(faktaBygger.faktaversjon() !in henvendelser.keys) { "Ugyldig forsøk på å opprette duplikat Versjon ider. id=${faktaBygger.faktaversjon()}" }
        henvendelser[faktaBygger.faktaversjon()] = this
    }

    fun fakta(
        person: Person,
        faktaUUID: UUID = UUID.randomUUID(),
    ): Fakta = faktaBygger.fakta(person, faktaUUID)

    fun prosess(
        person: Person,
        prosesstype: Prosesstype,
        prosessUUID: UUID,
        faktaUUID: UUID,
    ) = faktaBygger.prosess(person, prosesstype, prosessUUID, faktaUUID)

    class ProsessBygger(
        private val faktaBygger: FaktaBygger,
        private val prosess: Prosess,
        private val regeltre: Subsumsjon,
    ) {
        fun prosess(
            person: Person,
            prosessUUID: UUID = UUID.randomUUID(),
            faktaUUID: UUID = UUID.randomUUID(),
        ): Prosess {
            val fakta = faktaBygger.fakta(person, faktaUUID)
            val subsumsjon = regeltre.bygg(fakta)
            return prosess.bygg(prosessUUID, fakta, subsumsjon)
        }
    }

    class FaktaBygger(
        private val prototypeFakta: Fakta,
        internal val faktumNavBehov: FaktumNavBehov? = null,
        private val prosesser: MutableMap<Prosesstype, ProsessBygger> = mutableMapOf(),
    ) {
        fun fakta(
            person: Person,
            faktaUUID: UUID = UUID.randomUUID(),
        ): Fakta =
            prototypeFakta
                .bygg(person, faktaUUID)
                // TODO: Shim på veien til at Fakta eier NAV behov
                .also { fakta -> faktumNavBehov?.let { fakta.faktumNavBehov(faktumNavBehov) } }

        internal fun faktaversjon() = prototypeFakta.faktaversjon

        fun registrer() = Henvendelser(this)

        fun leggTilProsess(
            prosess: Prosess,
            regeltre: Subsumsjon,
        ) = ProsessBygger(this, prosess, regeltre).also { prosesser[prosess.type] = it }

        fun prosess(
            person: Person,
            prosesstype: Prosesstype,
            prosessUUID: UUID = UUID.randomUUID(),
            faktaUUID: UUID = UUID.randomUUID(),
        ) = prosesser
            .filterKeys { it.navn == prosesstype.navn }
            .values
            .firstOrNull()
            ?.prosess(person, prosessUUID, faktaUUID)
            ?: throw IllegalArgumentException("Ukjent prosesstype: $prosesstype")
    }
}
