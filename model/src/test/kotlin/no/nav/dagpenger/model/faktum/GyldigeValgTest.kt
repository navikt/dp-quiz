package no.nav.dagpenger.model.faktum

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class GyldigeValgTest {

    @Test
    fun `Skal ikke kunne kalle sjekk-metoden på seg selv`() {
        val gyldigeValg = GyldigeValg("valg1", "valg2")

        assertThrows<IllegalArgumentException> { gyldigeValg.sjekk(gyldigeValg) }
    }
}
