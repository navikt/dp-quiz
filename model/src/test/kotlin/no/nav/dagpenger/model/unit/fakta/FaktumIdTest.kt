package no.nav.dagpenger.model.unit.fakta

import no.nav.dagpenger.model.fakta.FaktumId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class FaktumIdTest {

    @Test
    fun equals() {
        assertEquals(FaktumId(1), FaktumId(1))
        assertEquals(FaktumId(1), FaktumId(1))
        assertNotEquals(FaktumId(1), FaktumId(2))
        assertNotEquals(FaktumId(1), Any())
        assertNotEquals(FaktumId(1), null)
    }

    @Test
    fun hash() {
        assertEquals(FaktumId(1).hashCode(), FaktumId(1).hashCode())
    }

    @Test
    fun `Generere ny indeks FaktumId`() {
        assertEquals("16.2", FaktumId(16).medIndeks(2).id)
    }

    @Test
    fun `invalid indeks`() {
        assertThrows<IllegalArgumentException> { FaktumId(16).medIndeks(2).medIndeks(3) }
        assertThrows<IllegalArgumentException> { FaktumId(0) }
        assertThrows<IllegalArgumentException> { FaktumId(16).medIndeks(0) }
    }
}
