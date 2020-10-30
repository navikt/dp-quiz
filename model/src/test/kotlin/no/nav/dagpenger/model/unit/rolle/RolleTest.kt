package no.nav.dagpenger.model.unit.rolle
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.ja
import no.nav.dagpenger.model.faktum.Fakta
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.helpers.testSøknad
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class RolleTest {
    @Test
    fun `Faktum må besvares av riktig rolle`() {
        val faktum = Fakta(ja nei "ja" id 1).testSøknad().let { it ja 1 }

        assertThrows<IllegalAccessError> { faktum.besvar(true, Rolle.saksbehandler) }
        assertThrows<IllegalAccessError> { faktum.besvar(true, Rolle.nav) }
        assertEquals(true, faktum.besvar(true, Rolle.søker).svar())
        assertEquals(false, faktum.besvar(false).svar())
    }
}
