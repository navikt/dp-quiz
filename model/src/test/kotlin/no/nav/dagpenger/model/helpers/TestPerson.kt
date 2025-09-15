package no.nav.dagpenger.model.helpers

import no.nav.dagpenger.model.faktum.Identer
import no.nav.dagpenger.model.faktum.Person

internal val testPerson =
    Person(
        Identer
            .Builder()
            .folkeregisterIdent("12020052345")
            .aktørId("aktørId")
            .build(),
    )
