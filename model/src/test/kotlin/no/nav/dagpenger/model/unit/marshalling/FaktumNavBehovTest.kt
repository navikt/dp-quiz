package no.nav.dagpenger.model.unit.marshalling

import no.nav.dagpenger.model.marshalling.FaktumNavBehov
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class FaktumNavBehovTest {
    @Test
    fun `skal kunne registere faktum til nav-behov mapper`() {
        val map = mapOf(1 to "1")
        val faktumNavBehov = FaktumNavBehov(map)
        assertEquals("1", faktumNavBehov[1])
        assertThrows<IllegalArgumentException> { faktumNavBehov[2] }
    }
}
