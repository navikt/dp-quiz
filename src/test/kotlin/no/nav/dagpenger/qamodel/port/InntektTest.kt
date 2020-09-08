package no.nav.dagpenger.qamodel.port

import no.nav.dagpenger.qamodel.port.Inntekt.Companion.daglig
import no.nav.dagpenger.qamodel.port.Inntekt.Companion.månedlig
import no.nav.dagpenger.qamodel.port.Inntekt.Companion.årlig
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class InntektTest {

    @Test
    fun equality() {
        assertEquals(1000.årlig, 1000.årlig)
        assertEquals(1000.årlig, 1000.0.årlig)
        assertNotEquals(1000.årlig, 2000.årlig)
        assertNotEquals(1000.årlig, Any())
        assertNotEquals(1000.årlig, null)
        assertEquals(2600.årlig, 10.daglig)
        assertEquals(2400.årlig, 200.månedlig)
        assertEquals(0.årlig, Inntekt.INGEN)
    }

    @Test
    fun `set operations`() {
        Assertions.assertTrue(1000.årlig in hashSetOf(1000.årlig))
        assertEquals(1, hashSetOf(1000.årlig, 1000.årlig).size)
    }

    @Test
    fun hash() {
        assertEquals(1000.årlig.hashCode(), 1000.årlig.hashCode())
        assertEquals(2600.årlig.hashCode(), 10.daglig.hashCode())
    }

    @Test
    fun arithmetic() {
        assertEquals(1000.årlig, +(1000.årlig))
        assertEquals(1000.årlig, 700.årlig + 300.årlig)
        assertEquals(1000.årlig, 1700.årlig - 700.årlig)
        assertEquals(-1000.årlig, -(1000.årlig))
        assertEquals(1000.årlig, 200.årlig * 5.0)
        assertEquals(1000.årlig, 5 * 200.årlig)
        assertEquals(200.årlig, 1000.årlig / 5)
        assertEquals(5.0, 1000.årlig.ratio(200.årlig))
        assertEquals(0.2, 200.årlig.ratio(1000.årlig))
    }

    @Test
    fun comparison() {
        assertTrue(1000.årlig > 200.årlig)
        assertTrue(1000.årlig >= 200.årlig)
        assertTrue(1000.årlig >= 1000.årlig)
        assertFalse(1000.årlig > 1000.årlig)
    }
}
