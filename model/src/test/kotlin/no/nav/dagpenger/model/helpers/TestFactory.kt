package no.nav.dagpenger.model.helpers

import no.nav.dagpenger.model.faktum.Fakta
import no.nav.dagpenger.model.faktum.Faktatype
import no.nav.dagpenger.model.faktum.Faktaversjon
import no.nav.dagpenger.model.faktum.Person
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.seksjon.Prosess
import no.nav.dagpenger.model.seksjon.Prosesstype
import no.nav.dagpenger.model.seksjon.Seksjon
import no.nav.dagpenger.model.subsumsjon.Subsumsjon
import no.nav.dagpenger.model.subsumsjon.TomSubsumsjon
import java.util.UUID

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
): Prosess = Prosess(testProsesstype(), this, *seksjon().toTypedArray(), rootSubsumsjon = subsumsjon).testProsess()

internal fun Prosess.testProsess(testPerson: Person? = null): Prosess {
    val fakta = testPerson?.let { this.fakta.bygg(it) } ?: this.fakta
    return this.bygg(UUID.randomUUID(), fakta, this.rootSubsumsjon.bygg(fakta))
}

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
