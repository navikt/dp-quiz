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
import org.junit.jupiter.api.Assertions.assertTrue
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
            it.besvar(fakta.dato(1), 2.januar)
            it.besvar(fakta.dato(2), 2.januar)
            it.besvar(fakta.dato(3), 2.januar)
            it.besvar(fakta.dato(4), 2.januar)
            it.besvar(fakta.dato(11), 2.januar)
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

        assertNesteSeksjon("inntekter", 9) {
            it.besvar(fakta.inntekt(7), 200000.årlig)
            it.besvar(fakta.inntekt(8), 50000.årlig)
            it.validerSvar()
        }

        // Vi har et resultat, men det er fortsatt seksjoner igjen
        assertFalse(fakta.resultat()!!)
        assertEquals(1, fakta.nesteSeksjoner().size)

        assertNesteSeksjon("godkjenn virkningstidspunkt", 7) {
            it.besvar(fakta.ja(13), false)
            it.validerSvar()
        }

        assertFalse(fakta.resultat()!!)

        // Om saksbehandler ikke godkjenner virkningstidspunkt kan ikke det føre til innvilgelse
        assertNesteSeksjon("godkjenn virkningstidspunkt", 7) {
            it.besvar(fakta.ja(13), false)
        }

        assertFalse(fakta.resultat()!!)
        fakta.inntekt(7).besvar(2000000.årlig)
        fakta.inntekt(8).besvar(2000000.årlig)
        assertFalse(fakta.resultat()!!)
        assertNesteSeksjon("godkjenn virkningstidspunkt", 7) {
            it.besvar(fakta.ja(13), true)
        }

        assertTrue(fakta.resultat()!!)

        // En dato som gjøre at søknadsfristen ikke er overholdt
        fakta.dato(2).besvar(1.januar)

        // gjør at virkningstidspunkt må godkjennes på nytt
        assertFalse(fakta.dato(13).erBesvart())

        // og når inntekt blir besvart på nytt
        assertNesteSeksjon("inntekter", 9) {
            it.besvar(fakta.ja(13), true)
            it.besvar(fakta.inntekt(7), 2000000.årlig)
            it.besvar(fakta.inntekt(8), 2000000.årlig)
        }

        // og det spiller ikke noe rolle at saksbehandler godkjenner ny virkningstidspunkt
        assertNesteSeksjon("godkjenn virkningstidspunkt", 7) {
            it.besvar(fakta.ja(13), true)
        }

        // Søknadsdato er fortsatt før virkningstidspunkt som burde gjøre at subsumsjonstreet svarer, NEI!
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
