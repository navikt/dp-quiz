package no.nav.dagpenger.quiz.mediator.soknad.dagpenger

import no.nav.dagpenger.quiz.mediator.soknad.verifiserFeltsammensetting
import org.junit.jupiter.api.Test

internal class BostedslandTest {

    @Test
    fun `Sjekk om faktasammensettingen har endret seg siden sist`() {
        Bostedsland.verifiserFeltsammensetting(1, 6001)
    }
}
