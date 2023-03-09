package no.nav.dagpenger.model.unit.seksjon

import no.nav.dagpenger.model.faktum.Inntekt.Companion.månedlig
import no.nav.dagpenger.model.helpers.EksempelSøknad
import no.nav.dagpenger.model.helpers.januar
import no.nav.dagpenger.model.seksjon.Prosess
import no.nav.dagpenger.model.seksjon.Seksjon
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class SeksjonTest {
    private lateinit var søknad: EksempelSøknad
    private lateinit var prosess: Prosess
    private lateinit var datofakta: Seksjon
    private lateinit var inntektfakta: Seksjon

    @BeforeEach
    fun setup() {
        søknad = EksempelSøknad()
        prosess = søknad.prosess
        datofakta = prosess[0]
        inntektfakta = prosess[1]
    }

    @Test
    fun `finne søknadprosess som skal spørres neste`() {
        assertEquals(datofakta, prosess.nesteSeksjoner().first())

        søknad.withProsess {
            bursdag67.besvar(31.januar)
            bursdag67.besvar(31.januar)
            sisteDagMedLønn.besvar(1.januar)
            assertEquals(datofakta, prosess.nesteSeksjoner().first())

            inntekt15G.besvar(100000.månedlig)
            inntekt3G.besvar(1000.månedlig)
            assertEquals(datofakta, prosess.nesteSeksjoner().first())

            ønsketdato.besvar(1.januar)
            søknadsdato.besvar(1.januar)
            dimisjonsdato.besvar(1.januar)
            assertEquals(inntektfakta, prosess.nesteSeksjoner().first())
        }
    }

    @Test
    fun `søknadprosess er en collection`() {
        assertEquals(2, prosess.size)
        assertEquals(10, prosess.flatten().size)
    }
}
