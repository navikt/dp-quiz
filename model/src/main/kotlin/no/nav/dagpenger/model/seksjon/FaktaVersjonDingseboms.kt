package no.nav.dagpenger.model.seksjon

import no.nav.dagpenger.model.faktum.Fakta
import no.nav.dagpenger.model.faktum.Faktatype
import no.nav.dagpenger.model.faktum.Faktaversjon
import no.nav.dagpenger.model.faktum.Person
import java.util.UUID

class FaktaVersjonDingseboms private constructor(
    private val bygger: Bygger,
) {
    companion object {
        val faktaversjoner = mutableMapOf<Faktaversjon, FaktaVersjonDingseboms>()
        fun siste(faktatype: Faktatype): Faktaversjon {
            return faktaversjoner.keys.filter { it.faktatype.id == faktatype.id }.maxByOrNull { it.versjon }
                ?: throw IllegalArgumentException("Det finnes ingen versjoner av type=$faktatype. Er den registrert?")
        }

        fun id(faktaversjon: Faktaversjon) =
            faktaversjoner[faktaversjon]
                ?: throw IllegalArgumentException("Det finnes ingen versjon med id $faktaversjon")
    }

    init {
        require(bygger.faktaversjon() !in faktaversjoner.keys) { "Ugyldig forsøk på å opprette duplikat Versjon ider" }
        faktaversjoner[bygger.faktaversjon()] = this
    }

    fun fakta(
        person: Person,
        faktaUUID: UUID = UUID.randomUUID(),
    ): Fakta =
        bygger.fakta(person, faktaUUID)

    class Bygger(
        private val prototypeFakta: Fakta,
    ) {
        fun fakta(
            person: Person,
            faktaUUID: UUID = UUID.randomUUID(),
        ): Fakta = prototypeFakta.bygg(person, prototypeFakta.faktaversjon, faktaUUID)

        internal fun faktaversjon() = prototypeFakta.faktaversjon
        fun registrer() = FaktaVersjonDingseboms(this)
    }
}
