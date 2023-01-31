package no.nav.dagpenger.model.seksjon

import no.nav.dagpenger.model.faktum.Fakta
import no.nav.dagpenger.model.faktum.HenvendelsesType
import no.nav.dagpenger.model.faktum.Person
import no.nav.dagpenger.model.faktum.Prosessnavn
import no.nav.dagpenger.model.marshalling.FaktumNavBehov
import no.nav.dagpenger.model.subsumsjon.Subsumsjon
import java.util.UUID

class Versjon private constructor(
    private val bygger: Bygger
) {
    val faktumNavBehov get() = bygger.faktumNavBehov

    companion object {
        val versjoner = mutableMapOf<HenvendelsesType, Versjon>()
        fun siste(prosessnavn: Prosessnavn): HenvendelsesType {
            return versjoner.keys.filter { it.prosessnavn.id == prosessnavn.id }.maxByOrNull { it.versjon }
                ?: throw IllegalArgumentException("Det finnes ingen versjoner!")
        }

        fun id(versjonId: HenvendelsesType) =
            versjoner[versjonId] ?: throw IllegalArgumentException("Det finnes ingen versjon med id $versjonId")
    }

    init {
        require(bygger.prosessversjon() !in versjoner.keys) { "Ugyldig forsøk på å opprette duplikat Versjon ider" }
        versjoner[bygger.prosessversjon()] = this
    }

    fun utredningsprosess(
        person: Person,
        uuid: UUID = UUID.randomUUID()
    ): Utredningsprosess =
        bygger.utredningsprosess(person, uuid)

    fun utredningsprosess(fakta: Fakta) =
        bygger.utredningsprosess(fakta)

    class Bygger(
        private val prototypeFakta: Fakta,
        private val prototypeSubsumsjon: Subsumsjon,
        private val utredningsprosess: Utredningsprosess,
        internal val faktumNavBehov: FaktumNavBehov? = null
    ) {
        fun utredningsprosess(
            person: Person,
            uuid: UUID = UUID.randomUUID()
        ): Utredningsprosess =
            utredningsprosess(prototypeFakta.bygg(person, prototypeFakta.henvendelsesType, uuid))

        fun utredningsprosess(
            fakta: Fakta
        ): Utredningsprosess {
            val subsumsjon = prototypeSubsumsjon.bygg(fakta)
            return utredningsprosess.bygg(fakta, subsumsjon)
        }

        internal fun prosessversjon() = prototypeFakta.henvendelsesType
        fun registrer() = Versjon(this)
    }
}
