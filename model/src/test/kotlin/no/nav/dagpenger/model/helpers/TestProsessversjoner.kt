package no.nav.dagpenger.model.helpers

import no.nav.dagpenger.model.faktum.Prosessnavn
import no.nav.dagpenger.model.faktum.Prosessversjon

enum class Testprosess(override val id: String) : Prosessnavn {
    PrototypeSøknad("prototypeSøknad"),
    Test("test"),
}
internal val testversjon = Prosessversjon(Testprosess.Test, 0)
