package no.nav.dagpenger.model.unit.rolle

import no.nav.dagpenger.model.fakta.FaktumNavn
import no.nav.dagpenger.model.fakta.Rolle
import no.nav.dagpenger.model.fakta.faktum
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class RolleTest {
    @Test
    fun `Faktum må besvares av riktig rolle`() {
        val faktum = FaktumNavn(1,"ja").faktum<Boolean>()
        assertThrows<IllegalAccessError> { faktum.besvar(true, Rolle.saksbehandler) }
        assertThrows<IllegalAccessError> { faktum.besvar(true, Rolle.nav) }
        assertEquals(true, faktum.besvar(true, Rolle.søker).svar())
        assertEquals(false, faktum.besvar(false).svar())
    }
}
