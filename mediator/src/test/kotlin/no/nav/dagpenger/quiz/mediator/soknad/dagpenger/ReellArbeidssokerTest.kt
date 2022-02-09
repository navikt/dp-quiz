package no.nav.dagpenger.quiz.mediator.soknad.dagpenger

import no.nav.dagpenger.quiz.mediator.soknad.verifiserFeltsammensetting
import org.junit.jupiter.api.Test

internal class ReellArbeidssokerTest {

    @Test
    fun `Sjekk om faktasammensettingen har endret seg siden sist`() {
        ReellArbeidssoker.verifiserFeltsammensetting(8, 36)
    }
}
