package no.nav.dagpenger.model.unit.faktum

import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.ja
import no.nav.dagpenger.model.faktum.Søknad
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class AvhengigFaktumTest {

    @Test
    fun `Resetter avhengige faktum`() {
        val søknad = Søknad(
            0,
            ja nei "f1" id 1,
            ja nei "f2" id 2 avhengerAv 1,
            ja nei "f3" id 3 avhengerAv 2
        )
        val ja1 = søknad.ja(1)
        val ja2 = søknad.ja(2)
        val ja3 = søknad.ja(3)

        ja1.besvar(true)
        ja2.besvar(true)
        ja3.besvar(true)
        assertTrue { søknad.all { it.erBesvart() } }

        ja1.besvar(false)
        assertFalse { ja2.erBesvart() }
        assertFalse { ja3.erBesvart() }
    }
}
