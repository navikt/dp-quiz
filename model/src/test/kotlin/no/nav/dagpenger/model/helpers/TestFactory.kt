package no.nav.dagpenger.model.helpers

import no.nav.dagpenger.model.fakta.Fakta
import no.nav.dagpenger.model.fakta.Rolle
import no.nav.dagpenger.model.subsumsjon.TomSubsumsjon
import no.nav.dagpenger.model.søknad.Seksjon
import no.nav.dagpenger.model.søknad.Søknad
import no.nav.dagpenger.model.søknad.Versjon

internal const val UNG_PERSON_FNR_2018 = "12020052345"

internal fun Fakta.søknad(): Søknad {
    return Versjon(
        1,
        this,
        TomSubsumsjon,
        mapOf(
            Versjon.Type.Web to Søknad(
                Seksjon(
                    "seksjon",
                    Rolle.søker,
                    *(this.map { it }.toTypedArray())
                )
            )
        )
    ).søknad(UNG_PERSON_FNR_2018, Versjon.Type.Web)
}
