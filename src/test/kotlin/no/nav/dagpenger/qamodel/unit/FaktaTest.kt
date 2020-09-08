package no.nav.dagpenger.qamodel.unit

import no.nav.dagpenger.qamodel.fakta.DatoStrategi
import no.nav.dagpenger.qamodel.fakta.DatoSvar
import no.nav.dagpenger.qamodel.fakta.Faktum
import no.nav.dagpenger.qamodel.fakta.Ja
import no.nav.dagpenger.qamodel.fakta.JaNeiStrategi
import no.nav.dagpenger.qamodel.fakta.Nei
import no.nav.dagpenger.qamodel.fakta.Ubesvart
import no.nav.dagpenger.qamodel.handling.Handling
import no.nav.dagpenger.qamodel.helpers.januar
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class SpørsmålTest {
    val sisteDagMedLønn = Faktum(
        "Siste dag du har lønn",
        DatoStrategi(
            object : Handling() {}
        )
    )
    val inntekt1_5G = Faktum(
        "Inntekt er lik eller over 1.5G siste 12 måneder",
        JaNeiStrategi(
            object : Handling() {},
            object : Handling() {}
        )
    )
    val inntekt3G = Faktum(
        "Inntekt er lik eller over 3G siste 3 år",
        JaNeiStrategi(
            object : Handling() {},
            object : Handling(inntekt1_5G) { }
        )
    )

    @Test
    fun `at vi kan endre svar på spørsmål som allerede er besvart`() {
        assertEquals(
            Ja(inntekt3G),
            inntekt3G.spør().besvar(true)
        )

        assertEquals(
            Nei(inntekt3G),
            inntekt3G.besvar(false)
        )
    }

    @Test
    fun `at vi kan svare på spørsmål som krever en dato som svar`() {
        assertEquals(
            DatoSvar(sisteDagMedLønn, 1.januar),
            sisteDagMedLønn.spør().besvar(1.januar)
        )
    }

    @Test
    fun `at spørsmål er markert som ubesvart fram til de er besvart`() {
        inntekt3G.also {
            assertEquals(Ubesvart(inntekt3G), it.svar())
            it.spør().besvar(true)
            assertEquals(Ja(inntekt3G), it.svar())
        }
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
