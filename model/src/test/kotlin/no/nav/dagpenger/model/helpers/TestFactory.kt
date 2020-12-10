package no.nav.dagpenger.model.helpers

import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.seksjon.Seksjon
import no.nav.dagpenger.model.seksjon.Søknadprosess
import no.nav.dagpenger.model.seksjon.Versjon
import no.nav.dagpenger.model.subsumsjon.TomSubsumsjon

internal fun Søknad.testSøknadprosess(): Søknadprosess {
    return Versjon.VersjonBygger(
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
