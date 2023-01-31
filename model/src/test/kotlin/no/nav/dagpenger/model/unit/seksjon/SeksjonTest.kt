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
import no.nav.dagpenger.model.seksjon.Utredningsprosess
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class SeksjonTest {
    private lateinit var utredningsprosess: Utredningsprosess
    private lateinit var datofakta: Seksjon
    private lateinit var inntektfakta: Seksjon

    @BeforeEach
    fun setup() {
        utredningsprosess = eksempelSøknad()
        datofakta = utredningsprosess[0]
        inntektfakta = utredningsprosess[1]
    }

    @Test
    fun `finne søknadprosess som skal spørres neste`() {
        assertEquals(datofakta, utredningsprosess.nesteSeksjoner().first())

        bursdag67.besvar(31.januar)
        sisteDagMedLønn.besvar(1.januar)
        assertEquals(datofakta, utredningsprosess.nesteSeksjoner().first())

        inntekt15G.besvar(100000.månedlig)
        inntekt3G.besvar(1000.månedlig)
        assertEquals(datofakta, utredningsprosess.nesteSeksjoner().first())

        ønsketdato.besvar(1.januar)
        søknadsdato.besvar(1.januar)
        dimisjonsdato.besvar(1.januar)
        assertEquals(inntektfakta, utredningsprosess.nesteSeksjoner().first())
    }

    @Test
    fun `søknadprosess er en collection`() {
        assertEquals(2, utredningsprosess.size)
        assertEquals(10, utredningsprosess.flatten().size)
    }
}
