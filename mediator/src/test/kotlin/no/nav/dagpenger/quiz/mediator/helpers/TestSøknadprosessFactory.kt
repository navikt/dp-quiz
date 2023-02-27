package no.nav.dagpenger.quiz.mediator.helpers

import no.nav.dagpenger.model.faktum.Fakta
import no.nav.dagpenger.model.faktum.Faktatype
import no.nav.dagpenger.model.faktum.Identer
import no.nav.dagpenger.model.faktum.Person
import no.nav.dagpenger.model.seksjon.Prosess
import no.nav.dagpenger.model.seksjon.Prosessversjon
import no.nav.dagpenger.model.seksjon.Seksjon
import no.nav.dagpenger.model.subsumsjon.Subsumsjon
import no.nav.dagpenger.quiz.mediator.soknad.Prosessfakta

internal fun Fakta.testSøknadprosess(
    faktatype: Faktatype = Prosessfakta.Dagpenger,
    subsumsjon: Subsumsjon,
    seksjon: Fakta.() -> List<Seksjon>,
): Prosess {
    return Prosessversjon.Bygger(
        faktatype,
        subsumsjon,
        Prosess(
            Testprosess.Test,
            *seksjon().toTypedArray(),
        ),
    ).utredningsprosess(testPerson)
}

internal val testPerson = Person(Identer.Builder().folkeregisterIdent("12020052345").aktørId("aktørId").build())
