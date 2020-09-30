package no.nav.dagpenger

import no.nav.dagpenger.model.fakta.Rolle
import no.nav.dagpenger.model.søknad.Seksjon
import no.nav.dagpenger.model.søknad.Søknad
import no.nav.dagpenger.regelverk.dimisjonsdato
import no.nav.dagpenger.regelverk.fødselsdato
import no.nav.dagpenger.regelverk.utestengt
import no.nav.dagpenger.regelverk.ønsketDato
import org.junit.jupiter.api.Test
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNotSame
import kotlin.test.assertSame

internal class SøknaderTest {

    @Test
    fun `hente eller opprette søknad`() {
        val søknader = InMemorySøknader {
            Søknad(
                Seksjon(Rolle.søker, ønsketDato, fødselsdato),
                Seksjon(Rolle.søker, dimisjonsdato),
                Seksjon(Rolle.søker, utestengt),
            )
        }
        val id = UUID.randomUUID()
        val søknad = søknader.søknad(id).also {
            assertEquals(3, it.size)
        }
        assertSame(søknad, søknader.søknad(id))
        assertNotSame(søknad, søknader.søknad(UUID.randomUUID()))
    }
}
