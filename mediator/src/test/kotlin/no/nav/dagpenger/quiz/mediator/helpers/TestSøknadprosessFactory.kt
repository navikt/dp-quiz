package no.nav.dagpenger.quiz.mediator.helpers

import no.nav.dagpenger.model.faktum.Fakta
import no.nav.dagpenger.model.faktum.Identer
import no.nav.dagpenger.model.faktum.Person
import no.nav.dagpenger.model.seksjon.Prosess
import no.nav.dagpenger.model.seksjon.Seksjon
import no.nav.dagpenger.model.seksjon.Versjon
import no.nav.dagpenger.model.subsumsjon.Subsumsjon

internal fun Fakta.testSøknadprosess(
    subsumsjon: Subsumsjon,
    seksjon: Fakta.() -> List<Seksjon>,
): Prosess {
    return Versjon.Bygger(
        this,
        subsumsjon,
        Prosess(
            Testprosess.Test,
            *seksjon().toTypedArray(),
        ),
    ).utredningsprosess(testPerson)
}

internal val testPerson = Person(Identer.Builder().folkeregisterIdent("12020052345").aktørId("aktørId").build())
