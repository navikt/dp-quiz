package no.nav.dagpenger.qamodel.unit

import no.nav.dagpenger.qamodel.fakta.Faktum
import no.nav.dagpenger.qamodel.helpers.januar
import no.nav.dagpenger.qamodel.regel.VirkningstidspunktRegel
import no.nav.dagpenger.qamodel.subsumsjon.Subsumsjon
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDate

internal class SubsumsjonsTest {

    val bursdag67 = Faktum<LocalDate>("Datoen du fyller 67")

    val søknadsdato = Faktum<LocalDate>("Datoen du søker om dagpenger")


    @Test
    fun ` `(){
        bursdag67.besvar(31.januar)
        søknadsdato.besvar(1.januar)
        assertEquals(true, Subsumsjon(VirkningstidspunktRegel, bursdag67, søknadsdato).konkluder())
    }
}