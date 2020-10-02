package no.nav.dagpenger.model.søknad

import no.nav.dagpenger.model.fakta.FaktumNavn
import no.nav.dagpenger.model.fakta.Rolle
import no.nav.dagpenger.model.fakta.faktum
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows

internal class SøknadTest {
    @Test
    fun `Fakta med dupliserte navn`() {
        val navn = FaktumNavn(3, "a")
        val faktum1 = navn.faktum(Boolean::class.java)
        val faktum2 = navn.faktum(Boolean::class.java)
        val seksjon1 = Seksjon(Rolle.søker, faktum1)
        val seksjon2 = Seksjon(Rolle.søker, faktum2)
        assertThrows<IllegalArgumentException> { Søknad(seksjon1, seksjon2) }
    }

    @Test
    fun `Samme faktum kan være i flere seksjoner`() {
        val navn = FaktumNavn(3, "a")
        val faktum1 = navn.faktum(Boolean::class.java)
        val seksjon1 = Seksjon(Rolle.søker, faktum1)
        val seksjon2 = Seksjon(Rolle.søker, faktum1)
        assertDoesNotThrow { Søknad(seksjon1, seksjon2) }
    }
}
