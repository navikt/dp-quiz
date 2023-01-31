package no.nav.dagpenger.quiz.mediator.helpers

import no.nav.dagpenger.model.faktum.Fakta
import no.nav.dagpenger.model.faktum.Identer
import no.nav.dagpenger.model.faktum.Person
import no.nav.dagpenger.model.seksjon.Seksjon
import no.nav.dagpenger.model.seksjon.Utredningsprosess
import no.nav.dagpenger.model.seksjon.Versjon
import no.nav.dagpenger.model.subsumsjon.Subsumsjon

internal fun Fakta.testSøknadprosess(
    subsumsjon: Subsumsjon,
    seksjon: Fakta.() -> List<Seksjon>
): Utredningsprosess {
    return Versjon.Bygger(
        this,
        subsumsjon,
        Utredningsprosess(
            *seksjon().toTypedArray()
        )
    ).søknadprosess(testPerson)
}

internal val testPerson = Person(Identer.Builder().folkeregisterIdent("12020052345").aktørId("aktørId").build())
