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

internal class AvslagPåMinsteinntektTest {
    private lateinit var søknadprosess: Søknadprosess

    @BeforeEach
    fun setup() {
        Postgres.withMigratedDb {
            søknadprosess = AvslagPåMinsteinntekt().søknadprosess(Person(Identer.Builder().folkeregisterIdent("123123123").build()))
        }
    }

    @Test
    fun `De som ikke oppfyller kravet til minsteinntekt får avslag`() {
        assertNesteSeksjon("datoer", 5) {
            it.besvar(søknadprosess.dato(1), 5.januar)
            it.besvar(søknadprosess.dato(2), 5.januar)
            it.besvar(søknadprosess.dato(3), 5.januar)
            it.besvar(søknadprosess.dato(4), 5.januar)
            it.besvar(søknadprosess.dato(11), 5.januar)
            it.validerSvar()
        }

        assertNesteSeksjon("egenNæring", 1) {
            it.besvar(søknadprosess.ja(6), false)
            it.validerSvar()
        }

        assertNesteSeksjon("statiske", 2) {
            it.besvar(søknadprosess.inntekt(9), 300000.årlig)
            it.besvar(søknadprosess.inntekt(10), 150000.årlig)
            it.validerSvar()
        }

        assertNesteSeksjon("verneplikt", 1) {
            it.besvar(søknadprosess.ja(12), false)
            it.validerSvar()
        }

        assertNull(søknadprosess.resultat())

        assertNesteSeksjon("inntekter", 2) {
            it.besvar(søknadprosess.inntekt(7), 200000.årlig)
            it.besvar(søknadprosess.inntekt(8), 50000.årlig)
            it.validerSvar()
        }

        // Vi har et resultat, men det er fortsatt seksjoner igjen
        assertFalse(søknadprosess.resultat()!!)
        assertEquals(1, søknadprosess.nesteSeksjoner().size)

        assertNesteSeksjon("godkjenn virkningstidspunkt", 1) {
            it.besvar(søknadprosess.ja(13), true)
        }

        assertFalse(søknadprosess.resultat()!!)

        // Om saksbehandler ikke godkjenner virkningstidspunkt kan ikke det føre til innvilgelse
        assertNesteSeksjon("godkjenn virkningstidspunkt", 1) {
            it.besvar(søknadprosess.ja(13), false)
        }
        assertFalse(søknadprosess.resultat()!!)

        // om vi endrer inntekt til å tilfredstille minimumsinntekt
        // vil det fortsatt ikke være innvilgelses pga virkningstidspunkt ikke er godkjent
        søknadprosess.inntekt(7).besvar(2000000.årlig)
        søknadprosess.inntekt(8).besvar(2000000.årlig)
        assertFalse(søknadprosess.resultat()!!)

        // Når vi godkjenner virkningstidspunkt vil det føre til innvilgelse
        assertNesteSeksjon("godkjenn virkningstidspunkt", 1) {
            it.besvar(søknadprosess.ja(13), true)
        }
        assertTrue(søknadprosess.resultat()!!)

        // om vi så endrer søknadstidspunkt til å være før virkningstidspunkt vil det ikke føre til innvilgelse
        søknadprosess.dato(11).besvar(4.januar)
        assertFalse(søknadprosess.resultat()!!)

        // selv om virkningstidspunkt godkjennes
        søknadprosess.ja(13).besvar(true)
        assertFalse(søknadprosess.resultat()!!)

        // Være sikker på at godkjenning av virkningstidspunkt resettes når de
        // underliggende datoene for virkningstidspunkt endres
        søknadprosess.dato(11).besvar(6.januar)
        assertFalse(søknadprosess.ja(13).erBesvart())
    }

    private fun assertNesteSeksjon(
        navn: String,
        antallFaktum: Int,
        block: (it: SvarSpion) -> Unit = {}
    ) {
        søknadprosess.nesteSeksjoner().also { seksjoner ->
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
