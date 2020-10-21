package no.nav.dagpenger

import no.nav.dagpenger.model.fakta.Rolle
import no.nav.dagpenger.model.søknad.Seksjon
import no.nav.dagpenger.model.søknad.Søknad
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import java.time.LocalDate
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNotSame
import kotlin.test.assertSame

internal class SøknaderTest {

    @Test
    fun `hente eller opprette søknad`() {
        val f = Dagpengefakta()

        val søknader = InMemorySøknader {
            Søknad(
                Seksjon("seksjon", Rolle.søker, f.ønsketDato),
                Seksjon("seksjon", Rolle.søker, f.dimisjonsdato),
            )
        }
        val id = UUID.randomUUID()
        val søknad = søknader.søknad(id).also {
            assertEquals(2, it.size)
        }
        assertSame(søknad, søknader.søknad(id))
        assertNotSame(søknad, søknader.søknad(UUID.randomUUID()))
    }

    @Test
    fun `kan finne grunnleggende faktum gjennom derivert faktum`() {
        val f = Dagpengefakta()

        val søknad =
            Søknad(
                Seksjon("seksjon", Rolle.søker, f.virkningstidspunkt),
            )

        val finnFaktum = søknad.faktum<LocalDate>("7")
        assertDoesNotThrow { finnFaktum }
        assertEquals("7", finnFaktum.id)
    }
}
