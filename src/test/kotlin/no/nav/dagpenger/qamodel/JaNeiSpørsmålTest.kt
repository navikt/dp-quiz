package no.nav.dagpenger.qamodel

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDate

internal class JaNeiSpørsmålTest {
    @Test
    fun `jaNei spørsmål`() {
        assertEquals(Ja(Fakta.inntekt3G),
                Fakta.inntekt3G.jaNei().svar(true))

        assertEquals(Nei(Fakta.inntekt3G),
                Fakta.inntekt3G.jaNei().svar(false))
    }

    @Test
    fun `dato spørsmål`() {
        assertEquals(DatoSvar(Fakta.sisteDagMedLønn, 1.januar),
                Fakta.sisteDagMedLønn.dato().svar(1.januar))
    }

}

private val Int.januar get() = LocalDate.of(2018, 1,this)