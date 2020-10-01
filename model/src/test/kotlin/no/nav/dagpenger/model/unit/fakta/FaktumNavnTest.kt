package no.nav.dagpenger.model.unit.fakta

import no.nav.dagpenger.model.fakta.FaktumNavn
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test

internal class FaktumNavnTest {

    @Test
    fun equals() {
        assertEquals(FaktumNavn(1, "ja"), FaktumNavn(1, "ja"))
        assertEquals(FaktumNavn(1, "ja"), FaktumNavn(1, "nei"))
        assertNotEquals(FaktumNavn(1, "ja"), FaktumNavn(2, "ja"))
        assertNotEquals(FaktumNavn(1, "ja"), "ja")
        assertNotEquals(FaktumNavn(1, "ja"), null)
    }

    @Test
    fun hash() {
        assertEquals(FaktumNavn(1, "ja").hashCode(), FaktumNavn(1, "nei").hashCode())
    }

    @Test
    fun `Generere ny FaktumNavn`() {
        assertEquals("16.2", FaktumNavn(16, "orginal").indeks(2).id)
    }
}
