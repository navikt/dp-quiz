package no.nav.dagpenger.model.helpers

import no.nav.dagpenger.model.faktum.FaktaVersjon
import no.nav.dagpenger.model.faktum.HenvendelsesType

enum class Testprosess(override val id: String) : HenvendelsesType {
    PrototypeSøknad("prototypeSøknad"),
    Test("test"),
}

const val testprosessversjon = 0
internal val testversjon = FaktaVersjon(Testprosess.Test, testprosessversjon)
