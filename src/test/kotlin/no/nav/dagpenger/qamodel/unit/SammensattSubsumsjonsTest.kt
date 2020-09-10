package no.nav.dagpenger.qamodel.unit

import no.nav.dagpenger.qamodel.helpers.bursdag67
import no.nav.dagpenger.qamodel.helpers.comp
import no.nav.dagpenger.qamodel.helpers.dimisjonsdato
import no.nav.dagpenger.qamodel.helpers.inntektSisteÅr
import no.nav.dagpenger.qamodel.helpers.januar
import no.nav.dagpenger.qamodel.helpers.sisteDagMedLønn
import no.nav.dagpenger.qamodel.helpers.søknadsdato
import no.nav.dagpenger.qamodel.helpers.ønsketdato
import no.nav.dagpenger.qamodel.port.Inntekt.Companion.månedlig
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class SammensattSubsumsjonsTest {

    @Test
    fun `neste fakta`() {
        assertEquals(10, comp.fakta().size)
        ønsketdato.besvar(2.januar)
        søknadsdato.besvar(2.januar)
        sisteDagMedLønn.besvar(1.januar)
        assertEquals(1, comp.nesteFakta().size)
        bursdag67.besvar(31.januar)
        assertEquals(6, comp.nesteFakta().size)
        inntektSisteÅr.besvar(100000.månedlig)
        dimisjonsdato.besvar(1.januar)
        assertEquals(4, comp.nesteFakta().size)
        assertEquals(10, comp.fakta().size)
        println(comp)
    }

    @Test
    fun `subsumsjon status`(){
        //comp[0]
    }
}
