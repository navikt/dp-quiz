package no.nav.dagpenger.model.helpers

import no.nav.dagpenger.model.faktum.Faktatype
import no.nav.dagpenger.model.faktum.Faktaversjon
import no.nav.dagpenger.model.seksjon.Prosesstype

enum class TestFakta(override val id: String) : Faktatype {
    PrototypeSøknad("prototypeSøknad"),
    Test("test"),
}

enum class TestProsesser(override val faktatype: Faktatype) : Prosesstype {
    PrototypeSøknad(TestFakta.PrototypeSøknad),
    Test(TestFakta.Test),
}

const val testprosessversjon = 0
internal val testversjon = Faktaversjon(TestFakta.Test, testprosessversjon)
