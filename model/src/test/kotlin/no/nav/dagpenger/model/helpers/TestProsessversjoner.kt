package no.nav.dagpenger.model.helpers

import no.nav.dagpenger.model.faktum.Faktatype
import no.nav.dagpenger.model.seksjon.Prosesstype

enum class TestFakta(
    override val id: String,
) : Faktatype {
    Test("test"),
}

enum class TestProsesser(
    override val navn: String,
    override val faktatype: Faktatype,
) : Prosesstype {
    Test("Test", TestFakta.Test),
}

const val testprosessversjon = 0
internal val testversjon = testFaktaversjon() // Faktaversjon(TestFakta.Test, testprosessversjon)
