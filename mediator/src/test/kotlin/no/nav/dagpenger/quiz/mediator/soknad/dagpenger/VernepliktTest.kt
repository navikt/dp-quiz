package no.nav.dagpenger.quiz.mediator.soknad.dagpenger

import no.nav.dagpenger.quiz.mediator.soknad.verifiserFeltsammensetting
import org.junit.jupiter.api.Test

internal class VernepliktTest {

    @Test
    fun `Sjekk om faktasammensettingen har endret seg siden sist`() {
        Verneplikt.verifiserFeltsammensetting(1, 7001)
    }
}
