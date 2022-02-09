package no.nav.dagpenger.quiz.mediator.soknad.dagpenger

import no.nav.dagpenger.quiz.mediator.soknad.verifiserFeltsammensetting
import org.junit.jupiter.api.Test

internal class BarnetilleggTest {

    @Test
    fun `Sjekk om faktasammensettingen har endret seg siden sist`() {
        Barnetillegg.verifiserFeltsammensetting(8, 8036)
    }
}
