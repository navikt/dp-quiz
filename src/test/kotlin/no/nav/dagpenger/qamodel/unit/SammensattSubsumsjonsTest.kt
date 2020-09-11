package no.nav.dagpenger.qamodel.unit

import no.nav.dagpenger.qamodel.helpers.bursdag67
import no.nav.dagpenger.qamodel.helpers.dimisjonsdato
import no.nav.dagpenger.qamodel.helpers.inntektSisteÅr
import no.nav.dagpenger.qamodel.helpers.januar
import no.nav.dagpenger.qamodel.helpers.sisteDagMedLønn
import no.nav.dagpenger.qamodel.helpers.subsumsjonRoot
import no.nav.dagpenger.qamodel.helpers.søknadsdato
import no.nav.dagpenger.qamodel.helpers.ønsketdato
import no.nav.dagpenger.qamodel.port.Inntekt.Companion.månedlig
import no.nav.dagpenger.qamodel.subsumsjon.Subsumsjon
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class SammensattSubsumsjonsTest {
    private lateinit var comp: Subsumsjon

    @BeforeEach
    fun setup() {
        comp = subsumsjonRoot()
    }

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
    fun `subsumsjon status`() {
        println(comp.subsumsjoner(ønsketdato))
        // assertEquals(emptyList<EnkelSubsumsjon>(), comp.subsumsjoner(ønsketdato))
    }
}
