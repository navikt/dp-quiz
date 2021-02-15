package no.nav.dagpenger.quiz.mediator.meldinger

import no.nav.dagpenger.quiz.mediator.meldinger.MinsteArbeidsinntektStrategi.Terskel
import no.nav.dagpenger.quiz.mediator.meldinger.MinsteArbeidsinntektStrategi.finnTerskel
import org.junit.jupiter.api.Test
import java.time.LocalDate
import kotlin.test.assertEquals

class MinstearbeidsinntektTerskelStrategiTest {

    @Test
    fun `skal returnere rett terskel`() {
        val virkningstidspunkt = LocalDate.of(2020, 12, 31)

        assertEquals(Terskel(1.5, 3.0), finnTerskel(virkningstidspunkt))
    }
}
