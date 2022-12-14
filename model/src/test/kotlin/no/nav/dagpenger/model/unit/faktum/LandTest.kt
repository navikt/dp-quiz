package no.nav.dagpenger.model.unit.faktum

import no.nav.dagpenger.model.faktum.Land
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

internal class LandTest {

    @Test
    fun `Har gyldige land`() {
        assertTrue(Land.gyldigeLand.isNotEmpty())
    }

    @Test
    fun validation() {
        assertThrows<IllegalArgumentException> { Land("NORGE") }
        assertThrows<IllegalArgumentException> { Land("DDD") }
    }

    @Test
    fun equality() {
        assertEquals(Land("NOR"), Land("NOR"))
        Land("SWE").let { land ->
            assertEquals(land, land)
        }
        assertNotEquals(Land("SWE"), Land("NOR"))
    }

    @Test
    fun `case insentivity`() {
        assertEquals(Land("NOR"), Land("nor"))
        assertEquals(Land("nOR"), Land("Nor"))
    }

    @Test
    fun `Må tillate PDL sin kode for ukjent land`() {
        assertDoesNotThrow { Land(Land.pdlKodeForUkjentLand) }
        assertDoesNotThrow { Land(Land.pdlKodeForUkjentLand.lowercase()) }
        assertDoesNotThrow { Land("xUk") }
    }

    @Test
    fun `Må tillate PDL sin kode for statsløs`() {
        assertDoesNotThrow { Land(Land.pdlKodeForStatsløs) }
        assertDoesNotThrow { Land(Land.pdlKodeForStatsløs.lowercase()) }
        assertDoesNotThrow { Land("xXx") }
    }
}
