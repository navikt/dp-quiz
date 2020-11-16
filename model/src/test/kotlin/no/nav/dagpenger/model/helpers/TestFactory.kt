package no.nav.dagpenger.model.helpers

import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.seksjon.Seksjon
import no.nav.dagpenger.model.seksjon.Søknadprosess
import no.nav.dagpenger.model.seksjon.Versjon
import no.nav.dagpenger.model.subsumsjon.TomSubsumsjon

internal const val UNG_PERSON_FNR_2018 = "12020052345"

internal fun Søknad.testSøknadprosess(): Søknadprosess {
    return Versjon(
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
    ).søknadprosess(UNG_PERSON_FNR_2018, Versjon.UserInterfaceType.Web)
}
