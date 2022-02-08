package no.nav.dagpenger.model.faktum

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

internal class LandTest {
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
}
