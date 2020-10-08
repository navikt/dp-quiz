package no.nav.dagpenger.model.unit.fakta

import no.nav.dagpenger.model.fakta.FaktumNavn
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class FaktumNavnTest {

    @Test
    fun equals() {
        assertEquals(FaktumNavn<String>(1, "ja"), FaktumNavn<String>(1, "ja"))
        assertEquals(FaktumNavn<String>(1, "ja"), FaktumNavn<String>(1, "nei"))
        assertNotEquals(FaktumNavn<String>(1, "ja"), FaktumNavn<String>(2, "ja"))
        assertNotEquals(FaktumNavn<String>(1, "ja"), "ja")
        assertNotEquals(FaktumNavn<String>(1, "ja"), null)

        // TODO: assertNotEquals(FaktumNavn<String>(1, "ja"), FaktumNavn<Int>(1, "ja"))
    }

    @Test
    fun hash() {
        assertEquals(FaktumNavn<Int>(1, "ja").hashCode(), FaktumNavn<Int>(1, "nei").hashCode())
    }

    @Test
    fun `Generere ny FaktumNavn`() {
        assertEquals("16.2", FaktumNavn<Int>(16, "orginal").indeks(2).id)
    }

    @Test
    fun `invalid indeks`() {
        assertThrows<IllegalArgumentException> { FaktumNavn<String>(16, "ss").indeks(2).indeks(3) }
        assertThrows<IllegalArgumentException> { FaktumNavn<String>(0, "a") }
        assertThrows<IllegalArgumentException> { FaktumNavn<String>(16, "ss").indeks(0) }
    }
}
