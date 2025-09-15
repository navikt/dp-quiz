package no.nav.dagpenger.quiz.mediator.helpers

import no.nav.dagpenger.model.faktum.Fakta
import no.nav.dagpenger.model.faktum.Faktatype
import no.nav.dagpenger.model.faktum.Faktaversjon
import no.nav.dagpenger.model.faktum.Identer
import no.nav.dagpenger.model.faktum.Person
import no.nav.dagpenger.model.marshalling.FaktumNavBehov
import no.nav.dagpenger.model.seksjon.Henvendelser
import no.nav.dagpenger.model.seksjon.Prosess
import no.nav.dagpenger.model.seksjon.Prosesstype
import no.nav.dagpenger.model.seksjon.Seksjon
import no.nav.dagpenger.model.subsumsjon.Subsumsjon
import java.util.UUID

internal fun Fakta.testSøknadprosess(
    faktatype: Faktatype = testFaktatype(),
    subsumsjon: Subsumsjon,
    seksjon: Fakta.() -> List<Seksjon>,
): Prosess =
    Henvendelser
        .testProsess(
            this,
            Prosess(testProsesstype(faktatype), *seksjon().toTypedArray()),
            subsumsjon,
        ).prosess(testPerson)

internal val testPerson =
    Person(
        Identer
            .Builder()
            .folkeregisterIdent("12020052345")
            .aktørId("aktørId")
            .build(),
    )

internal fun Henvendelser.Companion.testProsess(
    prototypeFakta: Fakta,
    prototypeProsess: Prosess,
    prototypeSubsumsjon: Subsumsjon,
    faktumNavBehov: FaktumNavBehov = FaktumNavBehov(),
): Henvendelser.ProsessBygger =
    Henvendelser
        .FaktaBygger(prototypeFakta, faktumNavBehov)
        .leggTilProsess(prototypeProsess, prototypeSubsumsjon)

internal fun testFaktatype() =
    object : Faktatype {
        override val id = UUID.randomUUID().toString()
    }

internal fun testFaktaversjon(faktatype: Faktatype = testFaktatype()) = Faktaversjon(faktatype, 1)

internal fun testProsesstype(faktatype: Faktatype = testFaktatype()) =
    object : Prosesstype {
        override val navn: String = UUID.randomUUID().toString()
        override val faktatype: Faktatype = faktatype
    }

internal val Prosesstype.faktaversjon: Faktaversjon
    get() = testFaktaversjon(faktatype)
