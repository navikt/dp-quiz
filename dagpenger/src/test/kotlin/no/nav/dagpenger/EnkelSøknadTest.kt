package no.nav.dagpenger

import no.nav.dagpenger.regelverk.inngangsvilkår
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow

internal class EnkelSøknadTest {
    @Test
    fun `test at søknaden kan lages og matcher subsumsjoner`() {
        val søknad = EnkelSøknad().søknad()
        assertTrue(søknad.size > 1)

        assertDoesNotThrow("Alle faktum i inngansvilkår må være til stede i søknaden") {
            inngangsvilkår.deepCopy(søknad)
        }
    }
}