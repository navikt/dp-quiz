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
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import soknad.AvslagPåMinsteinntekt

internal class AvslagPåMinsteinntektTest {
    private lateinit var fakta: Faktagrupper

    @BeforeEach
    fun setup() {
        Postgres.withMigratedDb {
            fakta = AvslagPåMinsteinntekt().faktagrupper("123123123")
        }
    }

    @Test
    fun `De som ikke oppfyller kravet til minsteinntekt får avslag`() {
        assertNesteSeksjon("datoer", 5) {
            it.besvar(fakta.dato(1), 1.januar)
            it.besvar(fakta.dato(2), 1.januar)
            it.besvar(fakta.dato(3), 1.januar)
            it.besvar(fakta.dato(4), 1.januar)
            it.besvar(fakta.dato(11), 1.januar)
            it.validerSvar()
        }

        assertNesteSeksjon("egenNæring", 1) {
            it.besvar(fakta.ja(6), false)
            it.validerSvar()
        }

        assertNesteSeksjon("statiske", 2) {
            it.besvar(fakta.inntekt(9), 300000.årlig)
            it.besvar(fakta.inntekt(10), 150000.årlig)
            it.validerSvar()
        }

        assertNesteSeksjon("verneplikt", 1) {
            it.besvar(fakta.ja(12), false)
            it.validerSvar()
        }

        assertNull(fakta.resultat())

        assertNesteSeksjon("inntekter", 4) {
            it.besvar(fakta.inntekt(7), 200000.årlig)
            it.besvar(fakta.inntekt(8), 50000.årlig)
            it.validerSvar()
        }

        // Vi har et resultat, men det er fortsatt seksjoner igjen
        assertFalse(fakta.resultat()!!)
        assertEquals(1, fakta.nesteSeksjoner().size)

        assertNesteSeksjon("godkjenn virkningstidspunkt", 2) {
            it.besvar(fakta.ja(13), true)
        }

        assertFalse(fakta.resultat()!!)

        // Vi må slutte å spørre om det samme hver gang
        assertEquals(0, fakta.nesteSeksjoner().size)

        // Om saksbehandler ikke godkjenner virkningstidspunkt kan ikke det føre til innvilgelse
        assertNesteSeksjon("godkjenn virkningstidspunkt", 2) {
            it.besvar(fakta.ja(13), false)
        }

        assertFalse(fakta.resultat()!!)
    }

    private fun assertNesteSeksjon(
        navn: String,
        antallFaktum: Int,
        block: (it: SvarSpion) -> Unit = {}
    ) {
        fakta.nesteSeksjoner().also { seksjoner ->
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
