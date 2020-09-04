package no.nav.dagpenger.qamodel.unit

import no.nav.dagpenger.qamodel.fakta.DatoStrategi
import no.nav.dagpenger.qamodel.fakta.DatoSvar
import no.nav.dagpenger.qamodel.fakta.Faktum
import no.nav.dagpenger.qamodel.fakta.Ja
import no.nav.dagpenger.qamodel.fakta.JaNeiStrategi
import no.nav.dagpenger.qamodel.fakta.Nei
import no.nav.dagpenger.qamodel.fakta.Ubesvart
import no.nav.dagpenger.qamodel.handling.Handling
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDate

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
    fun `spørsmål spørsmål`() {
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
    fun `dato spørsmål`() {
        assertEquals(
            DatoSvar(sisteDagMedLønn, 1.januar),
            sisteDagMedLønn.spør().besvar(1.januar)
        )
    }

    @Test
    fun `ubesvarte spørsmål`() {
        inntekt3G.also {
            assertEquals(Ubesvart(inntekt3G), it.svar())
            it.spør().besvar(true)
            assertEquals(Ja(inntekt3G), it.svar())
        }
    }

    @Test
    fun `ugyldige spørsmål`() {
        assertThrows<IllegalStateException> {
            sisteDagMedLønn.besvar(1.januar)
        }
    }

    @Test
    fun `kan ikke spørre allerede spurte spørsmål`() {
        assertThrows<IllegalStateException> {
            sisteDagMedLønn.spør().spør()
        }
    }
}

private val Int.januar get() = LocalDate.of(2018, 1, this)
