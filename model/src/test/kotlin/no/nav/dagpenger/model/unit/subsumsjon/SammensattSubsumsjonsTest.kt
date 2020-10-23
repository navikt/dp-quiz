package no.nav.dagpenger.model.unit.subsumsjon

import no.nav.dagpenger.model.fakta.Inntekt.Companion.månedlig
import no.nav.dagpenger.model.helpers.bursdag67
import no.nav.dagpenger.model.helpers.dimisjonsdato
import no.nav.dagpenger.model.helpers.eksempelSøknad
import no.nav.dagpenger.model.helpers.februar
import no.nav.dagpenger.model.helpers.inntektSisteÅr
import no.nav.dagpenger.model.helpers.januar
import no.nav.dagpenger.model.helpers.sisteDagMedLønn
import no.nav.dagpenger.model.helpers.søknadsdato
import no.nav.dagpenger.model.helpers.ønsketdato
import no.nav.dagpenger.model.subsumsjon.Subsumsjon
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class SammensattSubsumsjonsTest {
    private lateinit var comp: Subsumsjon

    @BeforeEach
    fun setup() {
        comp = eksempelSøknad().rootSubsumsjon
    }

    @Test
    fun `neste fakta`() {
        ønsketdato.besvar(2.januar)
        søknadsdato.besvar(2.januar)
        sisteDagMedLønn.besvar(1.januar)
        assertEquals(1, comp.nesteFakta().size)
        bursdag67.besvar(31.januar)
        assertEquals(5, comp.nesteFakta().size)
        inntektSisteÅr.besvar(100000.månedlig)
        dimisjonsdato.besvar(1.januar)
        assertEquals(3, comp.nesteFakta().size)
    }

    @Test
    fun `enkel subsumsjon resultater`() {
        assertEquals(null, comp[0][0].resultat())

        søknadsdato.besvar(1.januar)
        bursdag67.besvar(31.januar)
        assertEquals(true, comp[0][0].resultat())

        søknadsdato.besvar(1.februar)
        assertEquals(false, comp[0][0].resultat())
    }
}
