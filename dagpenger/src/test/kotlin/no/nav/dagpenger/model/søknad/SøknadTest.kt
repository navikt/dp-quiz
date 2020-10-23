package no.nav.dagpenger.model.søknad

import no.nav.dagpenger.model.fakta.FaktumNavn
import no.nav.dagpenger.model.fakta.Rolle
import no.nav.dagpenger.model.fakta.faktum
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

internal class SøknadTest {

    @Test
    fun `Samme faktum kan være i flere seksjoner`() {
        val navn = FaktumNavn(3, "a")
        val faktum1 = navn.faktum(Boolean::class.java)
        val seksjon1 = Seksjon("seksjon1", Rolle.søker, faktum1)
        val seksjon2 = Seksjon("seksjon2", Rolle.søker, faktum1)
        val søknad = assertDoesNotThrow { Søknad(seksjon1, seksjon2) }
        assertEquals(seksjon1, søknad.seksjon("seksjon1"))
        assertEquals(seksjon2, søknad.seksjon("seksjon2"))
        assertThrows<NoSuchElementException> { søknad.seksjon("xxxx") }
    }
}
