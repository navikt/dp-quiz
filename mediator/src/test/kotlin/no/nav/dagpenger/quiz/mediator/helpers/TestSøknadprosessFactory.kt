package no.nav.dagpenger.quiz.mediator.helpers

import no.nav.dagpenger.model.faktum.Identer
import no.nav.dagpenger.model.faktum.Person
import no.nav.dagpenger.model.faktum.Fakta
import no.nav.dagpenger.model.seksjon.Faktagrupper
import no.nav.dagpenger.model.seksjon.Seksjon
import no.nav.dagpenger.model.seksjon.Versjon
import no.nav.dagpenger.model.subsumsjon.Subsumsjon

internal fun Fakta.testSøknadprosess(
    subsumsjon: Subsumsjon,
    seksjon: Fakta.() -> List<Seksjon>
): Faktagrupper {
    return Versjon.Bygger(
        this,
        subsumsjon,
        mapOf(
            Versjon.UserInterfaceType.Web to Faktagrupper(
                *seksjon().toTypedArray()
            )
        )
    ).søknadprosess(testPerson, Versjon.UserInterfaceType.Web)
}

internal val testPerson = Person(Identer.Builder().folkeregisterIdent("12020052345").aktørId("aktørId").build())
