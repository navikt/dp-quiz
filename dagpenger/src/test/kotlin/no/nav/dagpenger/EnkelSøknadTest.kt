package no.nav.dagpenger

import no.nav.dagpenger.regelverk.inngangsvilkår
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertNotSame

internal class EnkelSøknadTest {
    @Test
    fun `test at søknaden kan lages og matcher subsumsjoner`() {
        val søknad = EnkelSøknad().søknad()
        assertTrue(søknad.size > 1)

        assertDoesNotThrow("Alle faktum i inngansvilkår må være til stede i søknaden") {
            inngangsvilkår.deepCopy(søknad)
        }
    }

    @Test
    fun `at vi får ny instans av faktum for hver søknad`(){
        val søknad1 = EnkelSøknad().søknad()
        val søknad2 = EnkelSøknad().søknad()

        assertNotSame(søknad1.finnFaktum<LocalDate>("1"), søknad2.finnFaktum<LocalDate>("1"))
    }
}
