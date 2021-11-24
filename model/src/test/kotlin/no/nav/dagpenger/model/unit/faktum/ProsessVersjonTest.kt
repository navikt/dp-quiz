package no.nav.dagpenger.model.unit.faktum

import no.nav.dagpenger.model.faktum.ProsessVersjon
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test

internal class ProsessVersjonTest {
    @Test
    fun `prosess med likt navn og versjon`() {
        val prosessVersjon = ProsessVersjon("A", 1)

        assertEquals(prosessVersjon, prosessVersjon)
        assertEquals(ProsessVersjon("A", 1), ProsessVersjon("A", 1))
        assertNotEquals(ProsessVersjon("A", 1), ProsessVersjon("B", 1))
        assertNotEquals(ProsessVersjon("A", 1), ProsessVersjon("A", 2))
    }
}
