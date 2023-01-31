package no.nav.dagpenger.model.helpers

import no.nav.dagpenger.model.faktum.Prosessnavn
import no.nav.dagpenger.model.faktum.HenvendelsesType

enum class Testprosess(override val id: String) : Prosessnavn {
    PrototypeSøknad("prototypeSøknad"),
    Test("test"),
}

const val testprosessversjon = 0
internal val testversjon = HenvendelsesType(Testprosess.Test, testprosessversjon)
