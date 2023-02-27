package no.nav.dagpenger.model.seksjon

import no.nav.dagpenger.model.faktum.Fakta
import no.nav.dagpenger.model.faktum.Faktatype
import no.nav.dagpenger.model.faktum.Person
import no.nav.dagpenger.model.marshalling.FaktumNavBehov
import no.nav.dagpenger.model.subsumsjon.Subsumsjon
import java.util.UUID

class Prosessversjon private constructor(
    private val bygger: Bygger,
) {
    val faktumNavBehov get() = bygger.faktumNavBehov

    companion object {
        val prosesstyper = mutableMapOf<Prosesstype, Prosessversjon>()

        fun id(prosesstype: Prosesstype) =
            prosesstyper.filterKeys { it.navn == prosesstype.navn }.values.firstOrNull()
                ?: throw IllegalArgumentException("Det finnes ingen versjon med id $prosesstype")
    }

    init {
        // require(bygger.prosesstype() !in versjoner.keys) { "Ugyldig forsøk på å opprette duplikat Versjon ider" }
        prosesstyper[bygger.prosesstype()] = this
    }

    fun utredningsprosess(fakta: Fakta) =
        bygger.utredningsprosess(fakta)

    fun utredningsprosess(person: Person, prosessUUID: UUID = UUID.randomUUID(), faktaUUID: UUID = UUID.randomUUID()) =
        bygger.utredningsprosess(person, prosessUUID, faktaUUID)

    class Bygger(
        private val faktatype: Faktatype,
        private val prototypeSubsumsjon: Subsumsjon,
        private val prosess: Prosess,
        internal val faktumNavBehov: FaktumNavBehov? = null,
    ) {
        fun utredningsprosess(
            person: Person,
            prosessUUID: UUID = UUID.randomUUID(),
            faktaUUID: UUID = UUID.randomUUID(),
        ): Prosess =
            utredningsprosess(FaktaVersjonDingseboms.id(FaktaVersjonDingseboms.siste(faktatype)).fakta(person, faktaUUID), prosessUUID)

        internal fun utredningsprosess(
            fakta: Fakta,
            prosessUUID: UUID = UUID.randomUUID(),
        ): Prosess {
            val subsumsjon = prototypeSubsumsjon.bygg(fakta)
            return prosess.bygg(prosessUUID, fakta, subsumsjon)
        }

        internal fun prosesstype() = prosess.type
        fun registrer() = Prosessversjon(this)
    }
}
