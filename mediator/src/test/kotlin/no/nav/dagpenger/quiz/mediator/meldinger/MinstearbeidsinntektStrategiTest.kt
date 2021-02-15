package no.nav.dagpenger.quiz.mediator.meldinger

import no.nav.dagpenger.quiz.mediator.meldinger.MinstearbeidsinntektFaktorStrategi.Faktor
import no.nav.dagpenger.quiz.mediator.meldinger.MinstearbeidsinntektFaktorStrategi.finnFaktor
import org.junit.jupiter.api.Test
import java.time.LocalDate
import kotlin.test.assertEquals

class MinstearbeidsinntektStrategiTest {

    @Test
    fun `skal returnere terskel i henhold til §4-4 `() {
        assertEquals(Faktor(1.5, 3.0), finnFaktor(LocalDate.of(2020, 3, 19)))
        assertEquals(Faktor(1.5, 3.0), finnFaktor(LocalDate.of(2020, 11, 1)))
        assertEquals(Faktor(1.5, 3.0), finnFaktor(LocalDate.of(2021, 7, 1)))
    }

    @Test
    fun `skal returnere rett ved første forskrift for korona periode`() {
        assertEquals(Faktor(0.75, 2.25), finnFaktor(LocalDate.of(2020, 3, 21)))
        assertEquals(Faktor(0.75, 2.25), finnFaktor(LocalDate.of(2020, 10, 30)))
    }

    @Test
    fun `skal returnere rett ved andre forskrift for korona periode`() {
        assertEquals(Faktor(0.75, 2.25), finnFaktor(LocalDate.of(2021, 2, 1)))
        assertEquals(Faktor(0.75, 2.25), finnFaktor(LocalDate.of(2021, 6, 30)))
    }
}
