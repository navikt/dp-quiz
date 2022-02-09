package no.nav.dagpenger.quiz.mediator.soknad.dagpenger

import no.nav.dagpenger.quiz.mediator.soknad.verifiserFeltsammensetting
import org.junit.jupiter.api.Test

internal class KoronaFortsattRettTest {

    @Test
    fun `Sjekk om faktasammensettingen har endret seg siden sist`() {
        KoronaFortsattRett.verifiserFeltsammensetting(2, 20003)
    }
}
