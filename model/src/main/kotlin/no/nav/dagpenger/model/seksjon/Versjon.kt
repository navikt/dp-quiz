package no.nav.dagpenger.model.seksjon

import no.nav.dagpenger.model.faktum.Fakta
import no.nav.dagpenger.model.faktum.Person
import no.nav.dagpenger.model.faktum.Prosessversjon
import no.nav.dagpenger.model.marshalling.FaktumNavBehov
import no.nav.dagpenger.model.subsumsjon.Subsumsjon
import java.util.UUID

class Versjon private constructor(
    private val bygger: Bygger
) {
    val faktumNavBehov get() = bygger.faktumNavBehov

    companion object {
        val versjoner = mutableMapOf<Prosessversjon, Versjon>()
        fun siste(henvendelsesType: Prosessversjon): Prosessversjon {
            return TODO("trengs ikke")
            /*return versjoner.keys.filter { it.henvendelsesType.id == henvendelsesType.id }.maxByOrNull { it.versjon }
                ?: throw IllegalArgumentException("Det finnes ingen versjoner!")*/
        }

        fun id(versjonId: Prosessversjon) =
            versjoner[versjonId] ?: throw IllegalArgumentException("Det finnes ingen versjon med id $versjonId")
    }

    init {
        require(bygger.prosessversjon() !in versjoner.keys) { "Ugyldig forsøk på å opprette duplikat Versjon ider" }
        versjoner[bygger.prosessversjon()] = this
    }

    fun utredningsprosess(
        person: Person,
        faktaUUID: UUID = UUID.randomUUID()
    ): Utredningsprosess =
        bygger.utredningsprosess(person, faktaUUID)

    fun utredningsprosess(fakta: Fakta) =
        bygger.utredningsprosess(fakta)

    class Bygger(
        private val prototypeFakta: Fakta,
        private val prototypeSubsumsjon: Subsumsjon,
        private val utredningsprosess: Utredningsprosess,
        internal val faktumNavBehov: FaktumNavBehov? = null,
        private val prosessversjon: Prosessversjon
    ) {
        fun utredningsprosess(
            person: Person,
            faktaUUID: UUID = UUID.randomUUID()
        ): Utredningsprosess =
            utredningsprosess(prototypeFakta.bygg(person, prototypeFakta.faktaVersjon, faktaUUID))

        fun utredningsprosess(
            fakta: Fakta
        ): Utredningsprosess {
            val subsumsjon = prototypeSubsumsjon.bygg(fakta)
            return utredningsprosess.bygg(fakta, subsumsjon)
        }

        internal fun prosessversjon() = prosessversjon
        fun registrer() = Versjon(this)
    }
}
