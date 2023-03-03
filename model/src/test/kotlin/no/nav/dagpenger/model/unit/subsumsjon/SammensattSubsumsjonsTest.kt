package no.nav.dagpenger.model.unit.subsumsjon

import no.nav.dagpenger.model.faktum.Inntekt.Companion.månedlig
import no.nav.dagpenger.model.faktum.Inntekt.Companion.årlig
import no.nav.dagpenger.model.helpers.EksempelSøknad
import no.nav.dagpenger.model.helpers.februar
import no.nav.dagpenger.model.helpers.januar
import no.nav.dagpenger.model.seksjon.Prosess
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class SammensattSubsumsjonsTest {
    private lateinit var søknad: EksempelSøknad
    private lateinit var comp: Prosess

    @BeforeEach
    fun setup() {
        søknad = EksempelSøknad()
        comp = søknad.prosess
    }

    @Test
    fun `neste fakta`() {
        søknad.withProsess {
            ønsketdato.besvar(2.januar)
            søknadsdato.besvar(2.januar)
            sisteDagMedLønn.besvar(1.januar)
            assertEquals(1, comp.nesteFakta().size)
            bursdag67.besvar(31.januar)
            assertEquals(2, comp.nesteFakta().size)
            inntektSiste3år.besvar(20000.månedlig)
            inntekt3G.besvar(1000000.årlig)
            assertEquals(2, comp.nesteFakta().size)
            inntektSisteÅr.besvar(10000.månedlig)
            inntekt15G.besvar(500000.årlig)
            assertEquals(1, comp.nesteFakta().size)
            dimisjonsdato.besvar(1.januar)
            assertEquals(0, comp.nesteFakta().size)
        }
    }

    @Test
    fun `enkel subsumsjon resultater`() {
        assertEquals(null, comp.rootSubsumsjon[0][0].resultat())

        søknad.withProsess {
            søknadsdato.besvar(1.januar)
            bursdag67.besvar(31.januar)
            assertEquals(true, comp.rootSubsumsjon[0][0].resultat())

            søknadsdato.besvar(1.februar)
            assertEquals(false, comp.rootSubsumsjon[0][0].resultat())
        }
    }
}
