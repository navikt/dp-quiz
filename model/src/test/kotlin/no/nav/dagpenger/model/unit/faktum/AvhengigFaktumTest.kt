package no.nav.dagpenger.model.unit.faktum

import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.boolsk
import no.nav.dagpenger.model.faktum.ProsessVersjon
import no.nav.dagpenger.model.faktum.Søknad
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class AvhengigFaktumTest {

    @Test
    fun `Resetter avhengige faktum`() {
        val søknad = Søknad(
            ProsessVersjon("test", 0),
            boolsk faktum "f1" id 1,
            boolsk faktum "f2" id 2 avhengerAv 1,
            boolsk faktum "f3" id 3 avhengerAv 2
        )
        val ja1 = søknad.boolsk(1)
        val ja2 = søknad.boolsk(2)
        val ja3 = søknad.boolsk(3)

        ja1.besvar(true)
        ja2.besvar(true)
        ja3.besvar(true)
        assertTrue { søknad.all { it.erBesvart() } }

        ja1.besvar(false)
        assertFalse { ja2.erBesvart() }
        assertFalse { ja3.erBesvart() }
    }
}
