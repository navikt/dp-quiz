package no.nav.dagpenger.quiz.mediator.soknad.dagpenger

import no.nav.dagpenger.model.faktum.Land
import no.nav.dagpenger.model.faktum.Periode
import no.nav.dagpenger.model.faktum.Prosessversjon
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.faktum.Tekst
import no.nav.dagpenger.quiz.mediator.helpers.testSøknadprosess
import no.nav.dagpenger.quiz.mediator.soknad.Prosess
import no.nav.dagpenger.quiz.mediator.soknad.dagpenger.EøsArbeidsforhold.`eos arbeidsforhold arbeidsgivernavn`
import no.nav.dagpenger.quiz.mediator.soknad.dagpenger.EøsArbeidsforhold.`eos arbeidsforhold land`
import no.nav.dagpenger.quiz.mediator.soknad.dagpenger.EøsArbeidsforhold.`eos arbeidsforhold personnummer`
import no.nav.dagpenger.quiz.mediator.soknad.dagpenger.EøsArbeidsforhold.`eos arbeidsforhold varighet`
import no.nav.dagpenger.quiz.mediator.soknad.verifiserFeltsammensetting
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDate

internal class EøsArbeidsforholdTest {

    @Test
    fun `Sjekk om faktasammensettingen har endret seg siden sist`() {
        EøsArbeidsforhold.verifiserFeltsammensetting(6, 54021)
    }

    @Test
    fun `Har arbeidet innenfor EØS de siste 36 mnd`() {
        val søknad = Søknad(Prosessversjon(Prosess.Dagpenger, -1), *EøsArbeidsforhold.fakta())
        val søknadprosess = søknad.testSøknadprosess(
            EøsArbeidsforhold.regeltre(søknad)
        )

        søknadprosess.boolsk(EøsArbeidsforhold.`eos arbeid siste 36 mnd`).besvar(false)
        assertEquals(true, søknadprosess.resultat())
        søknadprosess.boolsk(EøsArbeidsforhold.`eos arbeid siste 36 mnd`).besvar(true)
        assertEquals(null, søknadprosess.resultat())

        søknadprosess.generator(EøsArbeidsforhold.`eos arbeidsforhold`).besvar(1)
        søknadprosess.tekst("$`eos arbeidsforhold arbeidsgivernavn`.1").besvar(Tekst("CERN"))
        søknadprosess.land("$`eos arbeidsforhold land`.1").besvar(Land("CHE"))
        søknadprosess.tekst("$`eos arbeidsforhold personnummer`.1").besvar(Tekst("12345678901"))
        søknadprosess.periode("$`eos arbeidsforhold varighet`.1").besvar(
            Periode(
                fom = LocalDate.now().minusDays(50),
                tom = LocalDate.now()
            )
        )

        assertEquals(true, søknadprosess.resultat())
    }
}
