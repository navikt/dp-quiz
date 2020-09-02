package no.nav.dagpenger.qamodel

import java.time.LocalDate
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class SpørsmålTest {
    @Test
    fun `jaNei spørsmål`() {
        assertEquals(Ja(Fakta.inntekt3G),
                Fakta.inntekt3G.jaNei().besvar(true))

        assertEquals(Nei(Fakta.inntekt3G),
                Fakta.inntekt3G.jaNei().besvar(false))
    }

    @Test
    fun `dato spørsmål`() {
        assertEquals(DatoSvar(Fakta.sisteDagMedLønn, 1.januar),
                Fakta.sisteDagMedLønn.dato().besvar(1.januar))
    }

    @Test
    fun `ubesvarte spørsmål`() {
        Fakta.inntekt3G.jaNei().also {
            assertEquals(Ubesvart(Fakta.inntekt3G), it.svar())
            it.besvar(true)
            assertEquals(Ja(Fakta.inntekt3G), it.svar())
        }
    }
}

private val Int.januar get() = LocalDate.of(2018, 1, this)
