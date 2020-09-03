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
                Fakta.inntekt3G.jaNei().aktiver().besvar(true))

        assertEquals(Nei(Fakta.inntekt3G),
                Fakta.inntekt3G.jaNei().aktiver().besvar(false))
    }

    @Test
    fun `dato spørsmål`() {
        assertEquals(DatoSvar(Fakta.sisteDagMedLønn, 1.januar),
                Fakta.sisteDagMedLønn.dato().aktiver().besvar(1.januar))
    }

    @Test
    fun `ubesvarte spørsmål`() {
        Fakta.inntekt3G.jaNei().also {
            assertEquals(Ubesvart(Fakta.inntekt3G), it.svar())
            it.aktiver().besvar(true)
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
    fun `aktivere allerede aktive spørsmål`() {
        assertThrows<IllegalStateException> {
            Fakta.sisteDagMedLønn.dato().aktiver().aktiver()
        }
    }
}

private val Int.januar get() = LocalDate.of(2018, 1, this)
