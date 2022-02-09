package no.nav.dagpenger.quiz.mediator.soknad.dagpenger

import no.nav.dagpenger.quiz.mediator.soknad.verifiserFeltsammensetting
import org.junit.jupiter.api.Test

internal class TilleggsopplysningerTest {

    @Test
    fun `Sjekk om faktasammensettingen har endret seg siden sist`() {
        Tilleggsopplysninger.verifiserFeltsammensetting(1, 4001)
    }
}
