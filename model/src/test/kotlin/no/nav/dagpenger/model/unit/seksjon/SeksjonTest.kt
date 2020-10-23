package no.nav.dagpenger.model.unit.seksjon

import no.nav.dagpenger.model.fakta.Inntekt.Companion.månedlig
import no.nav.dagpenger.model.helpers.bursdag67
import no.nav.dagpenger.model.helpers.dimisjonsdato
import no.nav.dagpenger.model.helpers.eksempelSøknad
import no.nav.dagpenger.model.helpers.inntekt15G
import no.nav.dagpenger.model.helpers.inntekt3G
import no.nav.dagpenger.model.helpers.januar
import no.nav.dagpenger.model.helpers.sisteDagMedLønn
import no.nav.dagpenger.model.helpers.søknadsdato
import no.nav.dagpenger.model.helpers.ønsketdato
import no.nav.dagpenger.model.søknad.Seksjon
import no.nav.dagpenger.model.søknad.Søknad
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class SeksjonTest {
    private lateinit var søknad: Søknad
    private lateinit var datofakta: Seksjon
    private lateinit var inntektfakta: Seksjon

    @BeforeEach
    fun setup() {
        søknad = eksempelSøknad()
        datofakta = søknad[0]
        inntektfakta = søknad[1]
    }

    @Test
    fun `finne faktagrupper som skal spørres neste`() {
        assertEquals(datofakta, søknad.nesteSeksjon())

        bursdag67.besvar(31.januar)
        sisteDagMedLønn.besvar(1.januar)
        assertEquals(datofakta, søknad.nesteSeksjon())

        inntekt15G.besvar(100000.månedlig)
        inntekt3G.besvar(1000.månedlig)
        assertEquals(datofakta, søknad.nesteSeksjon())

        ønsketdato.besvar(1.januar)
        søknadsdato.besvar(1.januar)
        dimisjonsdato.besvar(1.januar)
        assertEquals(inntektfakta, søknad.nesteSeksjon())
    }

    @Test
    fun `søknad er en collection`() {
        assertEquals(2, søknad.size)
        assertEquals(10, søknad.flatten().size)
    }
}
