package no.nav.dagpenger.model.unit.faktum

import no.nav.dagpenger.model.faktum.Land
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

internal class LandTest {
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
