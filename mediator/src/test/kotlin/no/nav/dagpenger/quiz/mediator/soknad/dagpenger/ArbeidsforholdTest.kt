package no.nav.dagpenger.quiz.mediator.soknad.dagpenger

import no.nav.dagpenger.quiz.mediator.soknad.verifiserFeltsammensetting
import org.junit.jupiter.api.Test

internal class ArbeidsforholdTest {

    @Test
    fun `Sjekk om faktasammensettingen har endret seg siden sist`() {
        Arbeidsforhold.verifiserFeltsammensetting(34, 272595)
    }
}
