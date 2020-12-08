package no.nav.dagpenger.quiz.mediator.integration

import no.nav.dagpenger.model.faktum.Faktum
import no.nav.dagpenger.model.faktum.Identer
import no.nav.dagpenger.model.faktum.Inntekt.Companion.årlig
import no.nav.dagpenger.model.faktum.Person
import no.nav.dagpenger.model.faktum.TemplateFaktum
import no.nav.dagpenger.model.seksjon.Seksjon
import no.nav.dagpenger.model.seksjon.Søknadprosess
import no.nav.dagpenger.quiz.mediator.helpers.Postgres
import no.nav.dagpenger.quiz.mediator.helpers.januar
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntekt
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDate

internal class AvslagPåMinsteinntektTest {
    private lateinit var søknadprosess: Søknadprosess

    @BeforeEach
    fun setup() {
        Postgres.withMigratedDb {
            søknadprosess =
                AvslagPåMinsteinntekt().søknadprosess(Person(Identer.Builder().folkeregisterIdent("123123123").build()))
        }
    }

    @Test
    fun `De som ikke oppfyller kravet til minsteinntekt får avslag`() {
        assertNesteSeksjon("oppstart", 1) {
            it.besvar(søknadprosess.dato(20), 2.januar)
            it.validerSvar()
        }

        assertNesteSeksjon("datoer", 7) {
            it.besvar(søknadprosess.dato(1), 5.januar)
            it.besvar(søknadprosess.dato(2), 5.januar)
            it.besvar(søknadprosess.dato(3), 5.januar)
            it.besvar(søknadprosess.dato(10), 2.januar)
            it.besvar(søknadprosess.generator(16), 1)
            it.besvarGenerertFaktum(søknadprosess.dato("18.1"), 1.januar(2018))
            it.besvarGenerertFaktum(søknadprosess.dato("19.1"), 30.januar(2018))
            it.validerSvar()
        }

        assertNesteSeksjon("godkjenn virkningstidspunkt", 1) {
            // Virkningstidspunktet er fram i tid.
            // Vi må vente med å innvilge til vi har nye inntekter.

            // Sett søknadtidspunkt fram i tid for å teste resten
            it.besvar(søknadprosess.dato(20), 5.januar)
        }

        assertNesteSeksjon("fangstOgFisk", 1) {
            it.besvar(søknadprosess.ja(5), false)
            it.validerSvar()
        }

        assertNesteSeksjon("grunnbeløp", 2) {
            it.besvar(søknadprosess.inntekt(8), 300000.årlig)
            it.besvar(søknadprosess.inntekt(9), 150000.årlig)
            it.validerSvar()
        }

        assertNesteSeksjon("inntektsunntak", 2) {
            it.besvar(søknadprosess.ja(11), false)
            it.besvar(søknadprosess.ja(17), false)
            it.validerSvar()
        }

        assertNull(søknadprosess.resultat())

        assertNesteSeksjon("inntekter", 2) {
            it.besvar(søknadprosess.inntekt(6), 200000.årlig)
            it.besvar(søknadprosess.inntekt(7), 50000.årlig)
            it.validerSvar()
        }
        // Vi har et resultat, men det er fortsatt seksjoner igjen
        assertFalse(søknadprosess.resultat()!!)
        assertEquals(1, søknadprosess.nesteSeksjoner().size)

        assertNesteSeksjon("godkjenn virkningstidspunkt", 1) {
            it.besvar(søknadprosess.ja(12), true)
        }
        assertFalse(søknadprosess.resultat()!!)

        // Om saksbehandler ikke godkjenner virkningstidspunkt kan ikke det føre til innvilgelse
        assertNesteSeksjon("godkjenn virkningstidspunkt", 1) {
            it.besvar(søknadprosess.ja(12), false)
        }
        assertNull(søknadprosess.resultat())

        // "Reset" av modellen, for nullstille godkjenningen som er satt til false
        søknadprosess.dato(20).besvar(LocalDate.now())

        // om vi endrer inntekt til å tilfredstille minimumsinntekt
        // vil det fortsatt ikke være innvilgelses pga virkningstidspunkt ikke er godkjent
        søknadprosess.inntekt(6).besvar(2000000.årlig)
        søknadprosess.inntekt(7).besvar(2000000.årlig)
        assertTrue(søknadprosess.resultat()!!)

        // Vi ønsker kun opppgave til saksbehandler ved avslag
        assertNesteSeksjon("godkjenn virkningstidspunkt", 0)
        assertTrue(søknadprosess.resultat()!!)

        søknadprosess.dato("18.1").besvar(1.januar(2019))
        søknadprosess.dato("19.1").besvar(30.januar(2019))
        assertFalse(søknadprosess.resultat()!!)

        // Være sikker på at godkjenning av virkningtidspunkt resettes når de
        // underliggende datoene for virkningstidspunkt endres
        søknadprosess.dato(10).besvar(6.januar)
        assertFalse(søknadprosess.ja(12).erBesvart())
    }

    private fun assertNesteSeksjon(
        navn: String,
        forventetAntallFaktum: Int,
        block: (it: SvarSpion) -> Unit = {}
    ) {
        søknadprosess.nesteSeksjoner().also { seksjoner ->
            assertEquals(1, seksjoner.size)

            seksjoner.first().also { seksjon ->
                assertEquals(navn, seksjon.navn)
                assertEquals(forventetAntallFaktum, seksjon.size, "Antall faktum i seksjonen")
            }.also {
                SvarSpion(it.fakta()).also { spion ->
                    block(spion)
                }
            }
        }
    }

    // Sjekker at testen svarer på alle nødvendige faktum i hver seksjon
    private class SvarSpion(fakta: Set<Faktum<*>>) {
        var skalBesvares = fakta
            .filterNot { faktum -> faktum.erBesvart() }
            .map { it.id }
            .toMutableList()
        var besvarteFaktum = mutableListOf<String>()

        fun <R : Comparable<R>> besvar(f: Faktum<R>, svar: R) {
            besvarteFaktum.add(f.id)
            f.besvar(svar)
        }

        fun <R : Comparable<R>> besvarGenerertFaktum(f: Faktum<R>, svar: R) {
            skalBesvares.add(f.id)
            besvarteFaktum.add(f.id)
            f.besvar(svar)
        }

        fun validerSvar() =
            assertEquals(skalBesvares.sorted(), besvarteFaktum.sorted(), "Det er ubesvarte faktum i seksjonen")
    }

    private fun Seksjon.fakta() = this.filter { it !is TemplateFaktum }.toSet()
}
