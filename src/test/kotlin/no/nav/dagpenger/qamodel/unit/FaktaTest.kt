package no.nav.dagpenger.qamodel.unit

import no.nav.dagpenger.qamodel.fakta.DatoStrategi
import no.nav.dagpenger.qamodel.fakta.Faktum
import no.nav.dagpenger.qamodel.fakta.JaNeiStrategi
import no.nav.dagpenger.qamodel.handling.Handling
import no.nav.dagpenger.qamodel.helpers.dato
import no.nav.dagpenger.qamodel.helpers.ja
import no.nav.dagpenger.qamodel.helpers.januar
import no.nav.dagpenger.qamodel.helpers.nei
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDate

internal class FaktaTest {
    val sisteDagMedLønn = Faktum(
        "Siste dag du har lønn",
        DatoStrategi(
            object : Handling<LocalDate>() {}
        )
    )
    val inntekt1_5G = Faktum(
        "Inntekt er lik eller over 1.5G siste 12 måneder",
        JaNeiStrategi(
            object : Handling<Boolean>() {},
            object : Handling<Boolean>() {}
        )
    )
    val inntekt3G = Faktum(
        "Inntekt er lik eller over 3G siste 3 år",
        JaNeiStrategi(
            object : Handling<Boolean>() {},
            object : Handling<Boolean>(inntekt1_5G) {}
        )
    )

    @Test
    fun `at vi kan endre svar på spørsmål som allerede er besvart`() {
        assertEquals(
            inntekt3G.ja,
            inntekt3G.spør().besvar(true)
        )

        assertEquals(
            inntekt3G.nei,
            inntekt3G.besvar(false)
        )
    }

    @Test
    fun `at vi kan svare på spørsmål som krever en dato som svar`() {
        assertEquals(
            sisteDagMedLønn.dato,
            sisteDagMedLønn.spør().besvar(1.januar)
        )
    }

    @Test
    fun `at vi ikke kan besvare spørsmål som ikke er stilt`() {
        assertThrows<IllegalStateException> {
            sisteDagMedLønn.besvar(1.januar)
        }
    }

    @Test
    fun `at vi kan ikke stille spørsmål som allerede er stilt`() {
        assertThrows<IllegalStateException> {
            sisteDagMedLønn.spør().spør()
        }
    }
}
