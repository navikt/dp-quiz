package no.nav.dagpenger.model.helpers

import no.nav.dagpenger.model.faktum.Fakta
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.seksjon.Prosess
import no.nav.dagpenger.model.seksjon.Prosessversjon
import no.nav.dagpenger.model.seksjon.Seksjon
import no.nav.dagpenger.model.subsumsjon.Subsumsjon
import no.nav.dagpenger.model.subsumsjon.TomSubsumsjon

internal fun Fakta.testSøknadprosess(
    subsumsjon: Subsumsjon = TomSubsumsjon,
    seksjon: Fakta.() -> List<Seksjon> = {
        listOf(
            Seksjon(
                "seksjon",
                Rolle.søker,
                *(this.map { it }.toTypedArray()),
            ),
        )
    },
): Prosess {
    return Prosessversjon.Bygger(
        this,
        subsumsjon,
        // TODO: Denne må nok bli et argument
        Prosess(TestProsesser.Test, *seksjon().toTypedArray()),
    ).utredningsprosess(testPerson)
}
