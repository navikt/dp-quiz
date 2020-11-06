package integration

import helpers.Postgres
import helpers.januar
import no.nav.dagpenger.model.faktagrupper.Faktagrupper
import no.nav.dagpenger.model.faktagrupper.Seksjon
import no.nav.dagpenger.model.faktum.Faktum
import no.nav.dagpenger.model.faktum.Inntekt.Companion.årlig
import no.nav.dagpenger.model.faktum.TemplateFaktum
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import soknad.AvslagPåMinsteinntekt

internal class AvslagPåMinsteinntektTest {
    private lateinit var faktagrupper: Faktagrupper

    @BeforeEach
    fun setup() {
        Postgres.withMigratedDb {
            faktagrupper = AvslagPåMinsteinntekt().faktagrupper("123123123")
        }
    }

    @Test
    fun `De som ikke oppfyller kravet til minsteinntekt får avslag`() {
        faktagrupper.nesteSeksjoner().also { seksjoner ->
            assertEquals(1, seksjoner.size)

            seksjoner.first().also { seksjon ->
                assertEquals("datoer", seksjon.navn)
                assertEquals(5, seksjon.size)
            }
        }.also {
            faktagrupper.dato(1).besvar(1.januar)
            faktagrupper.dato(2).besvar(1.januar)
            faktagrupper.dato(3).besvar(1.januar)
            faktagrupper.dato(4).besvar(1.januar)
            faktagrupper.dato(11).besvar(1.januar)
        }

        faktagrupper.nesteSeksjoner().also { seksjoner ->
            assertEquals(1, seksjoner.size)

            seksjoner.first().also { seksjon ->
                assertEquals("egenNæring", seksjon.navn)
                assertEquals(1, seksjon.size)
            }
        }.also {
            faktagrupper.ja(6).besvar(false)
        }

        faktagrupper.nesteSeksjoner().also { seksjoner ->
            assertEquals(1, seksjoner.size)

            seksjoner.first().also { seksjon ->
                assertEquals("statiske", seksjon.navn)
                assertEquals(2, seksjon.size)
            }
        }.also {
            faktagrupper.inntekt(9).besvar(300000.årlig)
            faktagrupper.inntekt(10).besvar(150000.årlig)
        }

        faktagrupper.nesteSeksjoner().also { seksjoner ->
            assertEquals(1, seksjoner.size)

            seksjoner.first().also { seksjon ->
                assertEquals("verneplikt", seksjon.navn)
                assertEquals(1, seksjon.size)
            }
        }.also {
            faktagrupper.ja(12).besvar(false)
        }

        faktagrupper.nesteSeksjoner().also { seksjoner ->
            assertEquals(1, seksjoner.size)

            seksjoner.first().also { seksjon ->
                assertEquals("inntekter", seksjon.navn)
                assertEquals(4, seksjon.size)
            }
        }.also {
            faktagrupper.inntekt(7).besvar(200000.årlig)
            faktagrupper.inntekt(8).besvar(50000.årlig)
        }
        /*
        faktagrupper.nesteSeksjon().also { seksjon ->
            assertEquals("godkjenn virkningstidspunkt", seksjon.navn)
            assertEquals(1, seksjon.size)
        }.also {
            faktagrupper.ja(13).besvar(true, Rolle.saksbehandler)
        }
         */

        assertNotNull(faktagrupper.resultat())
        assertFalse(faktagrupper.resultat()!!)
    }

    private fun assertIder(fakta: Set<Faktum<*>>, vararg ider: Int) {
        assertIder(fakta, *(ider.map { it.toString() }.toTypedArray()))
    }

    private fun assertIder(fakta: Set<Faktum<*>>, vararg ider: String) {
        assertEquals(ider.toList(), fakta.map { it.id })
    }

    private fun Seksjon.fakta() = this.filter { it !is TemplateFaktum }.toSet()
}
