package no.nav.dagpenger.qamodel

import java.lang.IllegalStateException
import java.time.LocalDate
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class SpørsmålTest {
    @Test
    fun `jaNei spørsmål`() {
        assertEquals(Ja(Fakta.inntekt3G),
                Fakta.inntekt3G.jaNei().spør().besvar(true))

        assertEquals(Nei(Fakta.inntekt3G),
                Fakta.inntekt3G.jaNei().spør().besvar(false))
    }

    @Test
    fun `dato spørsmål`() {
        assertEquals(DatoSvar(Fakta.sisteDagMedLønn, 1.januar),
                Fakta.sisteDagMedLønn.dato().spør().besvar(1.januar))
    }

    @Test
    fun `ubesvarte spørsmål`() {
        Fakta.inntekt3G.jaNei().also {
            assertEquals(Ubesvart(Fakta.inntekt3G), it.svar())
            it.spør().besvar(true)
            assertEquals(Ja(Fakta.inntekt3G), it.svar())
        }
    }

    @Test
    fun `ugyldige spørsmål`() {
        assertThrows<IllegalStateException> {
            Fakta.sisteDagMedLønn.dato().besvar(1.januar)
        }
    }

    @Test
    fun `kan ikke spørre allerede spurte spørsmål`() {
        assertThrows<IllegalStateException> {
            Fakta.sisteDagMedLønn.dato().spør().spør()
        }
    }
}

private val Int.januar get() = LocalDate.of(2018, 1, this)
