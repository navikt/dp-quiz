package no.nav.dagpenger.quiz.mediator.soknad.dagpenger

import no.nav.dagpenger.quiz.mediator.soknad.verifiserFeltsammensetting
import org.junit.jupiter.api.Test

internal class AndreYtelserTest {

    @Test
    fun `Sjekk om faktasammensettingen har endret seg siden sist`() {
        AndreYtelser.verifiserFeltsammensetting(13, 65091)
    }
}
