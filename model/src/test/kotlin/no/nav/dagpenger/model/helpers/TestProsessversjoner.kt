package no.nav.dagpenger.model.helpers

import no.nav.dagpenger.model.faktum.Faktaversjon
import no.nav.dagpenger.model.faktum.Prosessnavn

enum class Testprosess(override val id: String) : Prosessnavn {
    PrototypeSøknad("prototypeSøknad"),
    Test("test"),
}

const val testprosessversjon = 0
internal val testversjon = Faktaversjon(Testprosess.Test, testprosessversjon)
