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
        assertNesteSeksjon("datoer", 5) {
            it.besvar(faktagrupper.dato(1), 1.januar)
            it.besvar(faktagrupper.dato(2), 1.januar)
            it.besvar(faktagrupper.dato(3), 1.januar)
            it.besvar(faktagrupper.dato(4), 1.januar)
            it.besvar(faktagrupper.dato(11), 1.januar)
            it.validerSvar()
        }

        assertNesteSeksjon("egenNæring", 1) {
            it.besvar(faktagrupper.ja(6), false)
            it.validerSvar()
        }

        assertNesteSeksjon("statiske", 2) {
            it.besvar(faktagrupper.inntekt(9), 300000.årlig)
            it.besvar(faktagrupper.inntekt(10), 150000.årlig)
            it.validerSvar()
        }

        assertNesteSeksjon("verneplikt", 1) {
            it.besvar(faktagrupper.ja(12), false)
            it.validerSvar()
        }

        assertNesteSeksjon("inntekter", 4) {
            it.besvar(faktagrupper.inntekt(7), 200000.årlig)
            it.besvar(faktagrupper.inntekt(8), 50000.årlig)
            it.validerSvar()
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

    private fun assertNesteSeksjon(
        navn: String,
        antallFaktum: Int,
        block: (it: SvarSpion) -> Unit = {}
    ) {
        faktagrupper.nesteSeksjoner().also { seksjoner ->
            assertEquals(1, seksjoner.size)

            seksjoner.first().also { seksjon ->
                assertEquals(navn, seksjon.navn)
                assertEquals(antallFaktum, seksjon.size)
            }.also {
                SvarSpion(it.fakta()).also { spion ->
                    block(spion)
                }
            }
        }
    }

    // Sjekker at testen svarer på alle nødvendige faktum i hver seksjon
    private class SvarSpion(fakta: Set<Faktum<*>>) {
        val skalBesvares = fakta.filterNot { faktum -> faktum.erBesvart() }.map { it.id }
        var besvarteFaktum = mutableListOf<String>()

        fun <R : Comparable<R>> besvar(f: Faktum<R>, svar: R) {
            besvarteFaktum.add(f.id)
            f.besvar(svar)
        }

        fun validerSvar() =
            assertEquals(skalBesvares.sorted(), besvarteFaktum.sorted(), "Det er ubesvarte faktum i seksjonen")
    }

    private fun Seksjon.fakta() = this.filter { it !is TemplateFaktum }.toSet()
}
