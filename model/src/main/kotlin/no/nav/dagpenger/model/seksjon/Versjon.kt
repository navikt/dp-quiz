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
        require(bygger.prosessVersjon() !in versjoner.keys) { "Ugyldig forsøk på å opprette duplikat Versjon ider" }
        versjoner[bygger.prosessVersjon()] = this
    }

    fun søknadprosess(
        person: Person,
        uuid: UUID = UUID.randomUUID()
    ): Faktagrupper =
        bygger.søknadprosess(person, uuid)

    fun søknadprosess(fakta: Fakta) =
        bygger.søknadprosess(fakta)

    class Bygger(
        private val prototypeFakta: Fakta,
        private val prototypeSubsumsjon: Subsumsjon,
        private val faktagrupper: Faktagrupper,
        internal val faktumNavBehov: FaktumNavBehov? = null
    ) {
        fun søknadprosess(
            person: Person,
            uuid: UUID = UUID.randomUUID()
        ): Faktagrupper =
            søknadprosess(prototypeFakta.bygg(person, prototypeFakta.prosessVersjon, uuid))

        fun søknadprosess(
            fakta: Fakta
        ): Faktagrupper {
            val subsumsjon = prototypeSubsumsjon.bygg(fakta)
            return faktagrupper.bygg(fakta, subsumsjon)
        }

        internal fun prosessVersjon() = prototypeFakta.prosessVersjon
        fun registrer() = Versjon(this)
    }
}
