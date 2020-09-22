package no.nav.dagpenger.model.unit.seksjon

import no.nav.dagpenger.model.fakta.Inntekt.Companion.månedlig
import no.nav.dagpenger.model.fakta.Rolle
import no.nav.dagpenger.model.helpers.bursdag67
import no.nav.dagpenger.model.helpers.dimisjonsdato
import no.nav.dagpenger.model.helpers.inntekt15G
import no.nav.dagpenger.model.helpers.inntekt3G
import no.nav.dagpenger.model.helpers.inntektSiste3år
import no.nav.dagpenger.model.helpers.inntektSisteÅr
import no.nav.dagpenger.model.helpers.januar
import no.nav.dagpenger.model.helpers.sisteDagMedLønn
import no.nav.dagpenger.model.helpers.subsumsjonRoot
import no.nav.dagpenger.model.helpers.søknadsdato
import no.nav.dagpenger.model.helpers.virkningstidspunkt
import no.nav.dagpenger.model.helpers.ønsketdato
import no.nav.dagpenger.model.subsumsjon.Subsumsjon
import no.nav.dagpenger.model.søknad.Seksjon
import no.nav.dagpenger.model.søknad.Søknad
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
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
        datofakta = Seksjon(Rolle.søker, bursdag67, sisteDagMedLønn, ønsketdato, søknadsdato, dimisjonsdato)
        inntektfakta = Seksjon(Rolle.søker, inntekt15G, inntekt3G, inntektSiste3år, inntektSisteÅr)
        søknad = Søknad(datofakta, inntektfakta)
    }

    @Test
    fun `finne faktagrupper som skal spørres neste`() {
        assertEquals(datofakta, søknad nesteSeksjon comp)

        bursdag67.besvar(31.januar)
        sisteDagMedLønn.besvar(1.januar)
        assertEquals(datofakta, søknad nesteSeksjon comp)

        inntekt15G.besvar(100000.månedlig)
        inntekt3G.besvar(1000.månedlig)
        assertEquals(datofakta, søknad nesteSeksjon comp)

        ønsketdato.besvar(1.januar)
        søknadsdato.besvar(1.januar)
        dimisjonsdato.besvar(1.januar)
        assertEquals(inntektfakta, søknad nesteSeksjon comp)
    }

    @Test
    fun `seksjon støtter utledede faktum`() {
        assertEquals(Seksjon(Rolle.søker, ønsketdato, sisteDagMedLønn, søknadsdato).size, Seksjon(Rolle.søker, virkningstidspunkt).size)

        assertTrue(Seksjon(Rolle.søker, virkningstidspunkt).contains(ønsketdato))
        assertFalse(Seksjon(Rolle.søker, virkningstidspunkt).contains(virkningstidspunkt))
        assertTrue(Seksjon(Rolle.søker, virkningstidspunkt).containsAll(listOf(ønsketdato, sisteDagMedLønn, søknadsdato)))
    }

    @Test
    fun `søknad er en collection`() {
        assertEquals(2, søknad.size)
        assertEquals(9, søknad.flatten().size)
    }
}
