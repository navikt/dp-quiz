package no.nav.dagpenger.quiz.mediator.helpers

import no.nav.dagpenger.model.faktum.Identer
import no.nav.dagpenger.model.faktum.Person
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.seksjon.Seksjon
import no.nav.dagpenger.model.seksjon.Søknadprosess
import no.nav.dagpenger.model.seksjon.Versjon
import no.nav.dagpenger.model.subsumsjon.Subsumsjon

internal fun Søknad.testSøknadprosess(subsumsjon: Subsumsjon): Søknadprosess {
    return Versjon.Bygger(
        this,
        subsumsjon,
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

internal val testPerson = Person(Identer.Builder().folkeregisterIdent("12020052345").aktørId("aktørId").build())
