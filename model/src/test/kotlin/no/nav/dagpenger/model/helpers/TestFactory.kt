package no.nav.dagpenger.model.helpers

import no.nav.dagpenger.model.faktum.Fakta
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.seksjon.Faktagrupper
import no.nav.dagpenger.model.seksjon.Seksjon
import no.nav.dagpenger.model.seksjon.Versjon
import no.nav.dagpenger.model.subsumsjon.Subsumsjon
import no.nav.dagpenger.model.subsumsjon.TomSubsumsjon

internal fun Fakta.testSøknadprosess(
    subsumsjon: Subsumsjon = TomSubsumsjon,
    seksjon: Fakta.() -> List<Seksjon> = {
        listOf(
            Seksjon(
                "seksjon",
                Rolle.søker,
                *(this.map { it }.toTypedArray())
            )
        )
    }
): Faktagrupper {
    return Versjon.Bygger(
        this,
        subsumsjon,
        Faktagrupper(*seksjon().toTypedArray())
    ).søknadprosess(testPerson)
}
