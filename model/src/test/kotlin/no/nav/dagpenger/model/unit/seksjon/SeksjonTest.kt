package no.nav.dagpenger.model.unit.seksjon

import no.nav.dagpenger.model.faktagrupper.Faktagrupper
import no.nav.dagpenger.model.faktagrupper.Seksjon
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
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class SeksjonTest {
    private lateinit var faktagrupper: Faktagrupper
    private lateinit var datofakta: Seksjon
    private lateinit var inntektfakta: Seksjon

    @BeforeEach
    fun setup() {
        faktagrupper = eksempelSøknad()
        datofakta = faktagrupper[0]
        inntektfakta = faktagrupper[1]
    }

    @Test
    fun `finne faktagrupper som skal spørres neste`() {
        assertEquals(datofakta, faktagrupper.nesteSeksjon())

        bursdag67.besvar(31.januar)
        sisteDagMedLønn.besvar(1.januar)
        assertEquals(datofakta, faktagrupper.nesteSeksjon())

        inntekt15G.besvar(100000.månedlig)
        inntekt3G.besvar(1000.månedlig)
        assertEquals(datofakta, faktagrupper.nesteSeksjon())

        ønsketdato.besvar(1.januar)
        søknadsdato.besvar(1.januar)
        dimisjonsdato.besvar(1.januar)
        assertEquals(inntektfakta, faktagrupper.nesteSeksjon())
    }

    @Test
    fun `faktagrupper er en collection`() {
        assertEquals(2, faktagrupper.size)
        assertEquals(10, faktagrupper.flatten().size)
    }
}
