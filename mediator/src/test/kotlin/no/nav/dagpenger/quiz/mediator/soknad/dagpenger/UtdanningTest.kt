package no.nav.dagpenger.quiz.mediator.soknad.dagpenger

import no.nav.dagpenger.quiz.mediator.soknad.verifiserFeltsammensetting
import org.junit.jupiter.api.Test

internal class UtdanningTest {

    @Test
    fun `Sjekk om faktasammensettingen har endret seg siden sist`() {
        Utdanning.verifiserFeltsammensetting(3, 6006)
    }
}
