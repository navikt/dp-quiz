package no.nav.dagpenger.model.unit.subsumsjon

import no.nav.dagpenger.model.fakta.GrunnleggendeFaktum
import no.nav.dagpenger.model.helpers.januar
import no.nav.dagpenger.model.regel.etter
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDate

internal class EnkelSubsumsjonsTest {

    val bursdag67 = GrunnleggendeFaktum<LocalDate>("Datoen du fyller 67")
    val søknadsdato = GrunnleggendeFaktum<LocalDate>("Datoen du søker om dagpenger")

    @Test
    fun `subsumsjonen kan konkludere`() {
        println(bursdag67 etter søknadsdato)

        // TODO: This should be return null, not throwing exception
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
