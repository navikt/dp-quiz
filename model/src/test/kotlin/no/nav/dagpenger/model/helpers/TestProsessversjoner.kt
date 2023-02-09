package no.nav.dagpenger.model.helpers

import no.nav.dagpenger.model.faktum.Faktatype
import no.nav.dagpenger.model.faktum.Faktaversjon

enum class Testprosess(override val id: String) : Faktatype {
    PrototypeSøknad("prototypeSøknad"),
    Test("test"),
}

const val testprosessversjon = 0
internal val testversjon = Faktaversjon(Testprosess.Test, testprosessversjon)
