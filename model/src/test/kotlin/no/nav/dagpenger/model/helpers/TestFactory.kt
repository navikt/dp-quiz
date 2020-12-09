package no.nav.dagpenger.model.helpers

import no.nav.dagpenger.model.faktum.Person
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.seksjon.Seksjon
import no.nav.dagpenger.model.seksjon.Søknadprosess
import no.nav.dagpenger.model.seksjon.Versjon
import no.nav.dagpenger.model.subsumsjon.Subsumsjon
import no.nav.dagpenger.model.subsumsjon.TomSubsumsjon
import java.util.UUID

internal fun Søknad.testSøknadprosess(): Søknadprosess {
    return SøknadprosessTestBygger(
        this,
        TomSubsumsjon,
        mapOf(
            Versjon.UserInterfaceType.Web to Søknadprosess(
                Seksjon(
                    "seksjon",
                    Rolle.søker,
                    *(this.map { it }.toTypedArray())
                )
            )
        )
    ).søknadprosess(testPerson, Versjon.UserInterfaceType.Web)
}

internal class SøknadprosessTestBygger(
    private val prototypeSøknad: Søknad,
    private val prototypeSubsumsjon: Subsumsjon,
    private val prototypeUserInterfaces: Map<Versjon.UserInterfaceType, Søknadprosess>
) {

    fun søknadprosess(person: Person, type: Versjon.UserInterfaceType, uuid: UUID = UUID.randomUUID()): Søknadprosess =
        søknadprosess(prototypeSøknad.bygg(person, prototypeSøknad.versjonId, uuid), type)

    fun søknadprosess(søknad: Søknad, type: Versjon.UserInterfaceType): Søknadprosess {
        val subsumsjon = prototypeSubsumsjon.bygg(søknad)
        return prototypeUserInterfaces[type]?.bygg(søknad, subsumsjon)
            ?: throw IllegalArgumentException("Kan ikke finne søknadprosess av type $type")
    }
}
