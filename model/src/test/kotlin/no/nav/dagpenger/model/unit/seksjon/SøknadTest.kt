package no.nav.dagpenger.model.unit.seksjon

import no.nav.dagpenger.model.fakta.Rolle
import no.nav.dagpenger.model.helpers.bursdag67
import no.nav.dagpenger.model.helpers.dimisjonsdato
import no.nav.dagpenger.model.helpers.inntekt15G
import no.nav.dagpenger.model.helpers.inntekt3G
import no.nav.dagpenger.model.helpers.inntektSiste3år
import no.nav.dagpenger.model.helpers.inntektSisteÅr
import no.nav.dagpenger.model.helpers.subsumsjonRoot
import no.nav.dagpenger.model.helpers.søknadsdato
import no.nav.dagpenger.model.helpers.virkningstidspunkt
import no.nav.dagpenger.model.helpers.ønsketdato
import no.nav.dagpenger.model.subsumsjon.Subsumsjon
import no.nav.dagpenger.model.søknad.Seksjon
import no.nav.dagpenger.model.søknad.Søknad
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class SøknadTest {
    private lateinit var comp: Subsumsjon
    private lateinit var datofakta: Seksjon
    private lateinit var inntektfakta: Seksjon
    private lateinit var søknad: Søknad

    @BeforeEach
    fun setup() {
        comp = subsumsjonRoot()
        datofakta = Seksjon(Rolle.søker, bursdag67, ønsketdato, søknadsdato, dimisjonsdato, virkningstidspunkt)
        inntektfakta = Seksjon(Rolle.søker, inntekt15G, inntekt3G, inntektSiste3år, inntektSisteÅr)
        søknad = Søknad(datofakta, inntektfakta)
    }
    @Test
    fun test() {
        assertEquals(10, søknad.faktaMap().size)
        println(søknad.faktaMap())
    }
}
