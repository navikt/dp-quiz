package no.nav.dagpenger.qamodel.unit

import no.nav.dagpenger.qamodel.fakta.Faktum
import no.nav.dagpenger.qamodel.helpers.januar
import no.nav.dagpenger.qamodel.subsumsjon.etter
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDate

internal class EnkelSubsumsjonsTest {

    val bursdag67 = Faktum<LocalDate>("Datoen du fyller 67")
    val søknadsdato = Faktum<LocalDate>("Datoen du søker om dagpenger")

    @Test
    fun `subsumsjonen kan konkludere`() {
        println(bursdag67 etter søknadsdato)

        assertThrows<IllegalStateException> { (bursdag67 etter søknadsdato).konkluder() }
        bursdag67.besvar(31.januar)
        søknadsdato.besvar(1.januar)
        assertTrue((bursdag67 etter søknadsdato).konkluder())

        println((bursdag67 etter søknadsdato))
    }

    @Test
    fun `subsumsjonen kan konkludere negativt`() {
        bursdag67.besvar(1.januar)
        søknadsdato.besvar(31.januar)
        assertFalse((bursdag67 etter søknadsdato).konkluder())
    }
}
