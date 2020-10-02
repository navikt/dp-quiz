package no.nav.dagpenger

import no.nav.dagpenger.model.fakta.Rolle
import no.nav.dagpenger.model.søknad.Seksjon
import no.nav.dagpenger.model.søknad.Søknad
import no.nav.dagpenger.regelverk.dimisjonsdato
import no.nav.dagpenger.regelverk.fødselsdato
import no.nav.dagpenger.regelverk.virkningstidspunkt
import no.nav.dagpenger.regelverk.ønsketDato
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
        val søknader = InMemorySøknader {
            Søknad(
                Seksjon(Rolle.søker, ønsketDato, fødselsdato),
                Seksjon(Rolle.søker, dimisjonsdato),
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
        val søknad =
            Søknad(
                Seksjon(Rolle.søker, virkningstidspunkt),
            )

        val finnFaktum = søknad.finnFaktum<LocalDate>("7")
        assertDoesNotThrow { finnFaktum }
        assertEquals("7", finnFaktum.id)
    }

    @Test
    fun `kan finne seksjon fra grunnleggende faktum gjennom derivert faktum`() {
        val søknad =
            Søknad(
                Seksjon(Rolle.søker, virkningstidspunkt),
            )

        assertDoesNotThrow { søknad.finnFaktum<LocalDate>("7").finnSeksjon(søknad) }
    }
}
