package no.nav.dagpenger.quiz.mediator.soknad.dagpenger

import no.nav.dagpenger.quiz.mediator.soknad.verifiserFeltsammensetting
import org.junit.jupiter.api.Test

internal class EøsArbeidsforholdTest {

    @Test
    fun `Sjekk om faktasammensettingen har endret seg siden sist`() {
        EøsArbeidsforhold.verifiserFeltsammensetting(6, 54021)
    }
}
