package no.nav.dagpenger.model.søknad

import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.ja
import no.nav.dagpenger.model.fakta.Fakta
import no.nav.dagpenger.model.fakta.Rolle
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

internal class SøknadTest {

    @Test
    fun `Samme faktum kan være i flere seksjoner`() {
        val fakta = Fakta(
            ja nei "a" id 3
        )
        val seksjon1 = Seksjon("seksjon1", Rolle.søker, fakta ja 3)
        val seksjon2 = Seksjon("seksjon2", Rolle.søker, fakta ja 3)
        val søknad = assertDoesNotThrow { Søknad(seksjon1, seksjon2) }
        assertEquals(seksjon1, søknad.seksjon("seksjon1"))
        assertEquals(seksjon2, søknad.seksjon("seksjon2"))
        assertThrows<NoSuchElementException> { søknad.seksjon("xxxx") }
    }
}
