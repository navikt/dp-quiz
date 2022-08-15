package no.nav.dagpenger.model.helpers

import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.seksjon.Seksjon
import no.nav.dagpenger.model.seksjon.Søknadprosess
import no.nav.dagpenger.model.seksjon.Versjon
import no.nav.dagpenger.model.subsumsjon.Subsumsjon
import no.nav.dagpenger.model.subsumsjon.TomSubsumsjon

internal fun Søknad.testSøknadprosess(
    subsumsjon: Subsumsjon = TomSubsumsjon,
    seksjon: Søknad.() -> List<Seksjon> = {
        listOf(
            Seksjon(
                "seksjon",
                Rolle.søker,
                *(this.map { it }.toTypedArray())
            )
        )
    }
): Søknadprosess {
    return Versjon.Bygger(
        this,
        subsumsjon,
        mapOf(
            Versjon.UserInterfaceType.Web to Søknadprosess(
                *seksjon().toTypedArray()
            )
        )
    ).søknadprosess(testPerson, Versjon.UserInterfaceType.Web)
}
