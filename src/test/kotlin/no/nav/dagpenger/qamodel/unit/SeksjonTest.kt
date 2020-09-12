package no.nav.dagpenger.qamodel.unit

import no.nav.dagpenger.qamodel.fakta.Inntekt.Companion.månedlig
import no.nav.dagpenger.qamodel.helpers.bursdag67
import no.nav.dagpenger.qamodel.helpers.dimisjonsdato
import no.nav.dagpenger.qamodel.helpers.inntekt15G
import no.nav.dagpenger.qamodel.helpers.inntekt3G
import no.nav.dagpenger.qamodel.helpers.inntektSiste3år
import no.nav.dagpenger.qamodel.helpers.inntektSisteÅr
import no.nav.dagpenger.qamodel.helpers.januar
import no.nav.dagpenger.qamodel.helpers.sisteDagMedLønn
import no.nav.dagpenger.qamodel.helpers.subsumsjonRoot
import no.nav.dagpenger.qamodel.helpers.søknadsdato
import no.nav.dagpenger.qamodel.helpers.ønsketdato
import no.nav.dagpenger.qamodel.subsumsjon.Subsumsjon
import no.nav.dagpenger.qamodel.søknad.Seksjon
import no.nav.dagpenger.qamodel.søknad.Søknad
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class SeksjonTest {
    private lateinit var comp: Subsumsjon
    private lateinit var datofakta: Seksjon
    private lateinit var inntektfakta: Seksjon
    private lateinit var søknad: Søknad

    @BeforeEach
    fun setup() {
        comp = subsumsjonRoot()
        datofakta = Seksjon(bursdag67, sisteDagMedLønn, ønsketdato, søknadsdato, dimisjonsdato)
        inntektfakta = Seksjon(inntekt15G, inntekt3G, inntektSiste3år, inntektSisteÅr)
        søknad = Søknad(datofakta, inntektfakta)
    }

    @Test
    fun `finne faktagrupper som skal spørres neste`() {
        assertEquals(datofakta, søknad nesteSeksjon comp)

        bursdag67 besvar 31.januar
        sisteDagMedLønn besvar 1.januar
        assertEquals(datofakta, søknad nesteSeksjon comp)

        inntekt15G besvar 100000.månedlig
        inntekt3G besvar 1000.månedlig
        assertEquals(datofakta, søknad nesteSeksjon comp)

        ønsketdato besvar 1.januar
        søknadsdato besvar 1.januar
        dimisjonsdato besvar 1.januar
        assertEquals(inntektfakta, søknad nesteSeksjon comp)
    }
}
