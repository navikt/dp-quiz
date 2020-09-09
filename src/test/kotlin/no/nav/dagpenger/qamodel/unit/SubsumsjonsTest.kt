package no.nav.dagpenger.qamodel.unit

import no.nav.dagpenger.qamodel.fakta.Faktum
import no.nav.dagpenger.qamodel.helpers.januar
import no.nav.dagpenger.qamodel.regel.Under67Regel
import no.nav.dagpenger.qamodel.subsumsjon.Subsumsjon
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDate

internal class SubsumsjonsTest {

    val bursdag67 = Faktum<LocalDate>("Datoen du fyller 67")
    val søknadsdato = Faktum<LocalDate>("Datoen du søker om dagpenger")

    @Test
    fun `subsumsjonen kan konkludere`(){
        println(Subsumsjon(Under67Regel, bursdag67, søknadsdato))

        assertThrows<IllegalStateException>{Subsumsjon(Under67Regel, bursdag67, søknadsdato).konkluder()}
        bursdag67.besvar(31.januar)
        søknadsdato.besvar(1.januar)
        assertTrue(Subsumsjon(Under67Regel, bursdag67, søknadsdato).konkluder())

        println(Subsumsjon(Under67Regel, bursdag67, søknadsdato))

    }

    @Test
    fun `subsumsjonen kan konkludere negativt`(){
        bursdag67.besvar(1.januar)
        søknadsdato.besvar(31.januar)
        assertFalse(Subsumsjon(Under67Regel, bursdag67, søknadsdato).konkluder())
    }
}