package no.nav.dagpenger.model.unit.seksjon

import no.nav.dagpenger.model.faktum.Inntekt.Companion.månedlig
import no.nav.dagpenger.model.helpers.bursdag67
import no.nav.dagpenger.model.helpers.dimisjonsdato
import no.nav.dagpenger.model.helpers.eksempelSøknad
import no.nav.dagpenger.model.helpers.inntekt15G
import no.nav.dagpenger.model.helpers.inntekt3G
import no.nav.dagpenger.model.helpers.januar
import no.nav.dagpenger.model.helpers.sisteDagMedLønn
import no.nav.dagpenger.model.helpers.søknadsdato
import no.nav.dagpenger.model.helpers.ønsketdato
import no.nav.dagpenger.model.seksjon.Seksjon
import no.nav.dagpenger.model.seksjon.Søknadprosess
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class SeksjonTest {
    private lateinit var søknadprosess: Søknadprosess
    private lateinit var datofakta: Seksjon
    private lateinit var inntektfakta: Seksjon

    @BeforeEach
    fun setup() {
        søknadprosess = eksempelSøknad()
        datofakta = søknadprosess[0]
        inntektfakta = søknadprosess[1]
    }

    @Test
    fun `finne søknadprosess som skal spørres neste`() {
        assertEquals(datofakta, søknadprosess.nesteSeksjoner().first())

        bursdag67.besvar(31.januar)
        sisteDagMedLønn.besvar(1.januar)
        assertEquals(datofakta, søknadprosess.nesteSeksjoner().first())

        inntekt15G.besvar(100000.månedlig)
        inntekt3G.besvar(1000.månedlig)
        assertEquals(datofakta, søknadprosess.nesteSeksjoner().first())

        ønsketdato.besvar(1.januar)
        søknadsdato.besvar(1.januar)
        dimisjonsdato.besvar(1.januar)
        assertEquals(inntektfakta, søknadprosess.nesteSeksjoner().first())
    }

    @Test
    fun `søknadprosess er en collection`() {
        assertEquals(2, søknadprosess.size)
        assertEquals(10, søknadprosess.flatten().size)
    }
}
