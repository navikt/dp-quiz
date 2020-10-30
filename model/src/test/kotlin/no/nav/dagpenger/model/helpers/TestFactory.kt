package no.nav.dagpenger.model.helpers

import no.nav.dagpenger.model.faktagrupper.Faktagrupper
import no.nav.dagpenger.model.faktagrupper.Seksjon
import no.nav.dagpenger.model.faktagrupper.Versjon
import no.nav.dagpenger.model.faktum.Fakta
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.subsumsjon.TomSubsumsjon

internal const val UNG_PERSON_FNR_2018 = "12020052345"

internal fun Fakta.testSøknad(): Faktagrupper {
    return Versjon(
        1,
        this,
        TomSubsumsjon,
        mapOf(
            Versjon.FaktagrupperType.Web to Faktagrupper(
                Seksjon(
                    "seksjon",
                    Rolle.søker,
                    *(this.map { it }.toTypedArray())
                )
            )
        )
    ).faktagrupper(UNG_PERSON_FNR_2018, Versjon.FaktagrupperType.Web)
}
