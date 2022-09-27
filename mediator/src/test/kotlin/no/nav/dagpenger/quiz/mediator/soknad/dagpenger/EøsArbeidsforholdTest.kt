package no.nav.dagpenger.quiz.mediator.soknad.dagpenger

import no.nav.dagpenger.model.faktum.Land
import no.nav.dagpenger.model.faktum.Periode
import no.nav.dagpenger.model.faktum.Prosessversjon
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.faktum.Tekst
import no.nav.dagpenger.quiz.mediator.helpers.testSøknadprosess
import no.nav.dagpenger.quiz.mediator.soknad.Prosess
import no.nav.dagpenger.quiz.mediator.soknad.dagpenger.EøsArbeidsforhold.`eøs arbeidsforhold arbeidsgivernavn`
import no.nav.dagpenger.quiz.mediator.soknad.dagpenger.EøsArbeidsforhold.`eøs arbeidsforhold land`
import no.nav.dagpenger.quiz.mediator.soknad.dagpenger.EøsArbeidsforhold.`eøs arbeidsforhold personnummer`
import no.nav.dagpenger.quiz.mediator.soknad.dagpenger.EøsArbeidsforhold.`eøs arbeidsforhold varighet`
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
        ) {
            EøsArbeidsforhold.seksjon(this)
        }

        søknadprosess.boolsk(EøsArbeidsforhold.`eøs arbeid siste 36 mnd`).besvar(false)
        assertEquals(true, søknadprosess.resultat())
        søknadprosess.boolsk(EøsArbeidsforhold.`eøs arbeid siste 36 mnd`).besvar(true)
        assertEquals(null, søknadprosess.resultat())

        søknadprosess.generator(EøsArbeidsforhold.`eøs arbeidsforhold`).besvar(2)
        søknadprosess.tekst("$`eøs arbeidsforhold arbeidsgivernavn`.1").besvar(Tekst("CERN"))
        søknadprosess.land("$`eøs arbeidsforhold land`.1").besvar(Land("CHE"))
        søknadprosess.tekst("$`eøs arbeidsforhold personnummer`.1").besvar(Tekst("12345678901"))
        søknadprosess.periode("$`eøs arbeidsforhold varighet`.1").besvar(
            Periode(
                fom = LocalDate.now().minusDays(50),
                tom = LocalDate.now()
            )
        )

        assertEquals(null, søknadprosess.resultat())

        søknadprosess.tekst("$`eøs arbeidsforhold arbeidsgivernavn`.2").besvar(Tekst("CERN"))
        søknadprosess.land("$`eøs arbeidsforhold land`.2").besvar(Land("CHE"))
        søknadprosess.tekst("$`eøs arbeidsforhold personnummer`.2").besvar(Tekst("12345678901"))
        søknadprosess.periode("$`eøs arbeidsforhold varighet`.2").besvar(
            Periode(
                fom = LocalDate.now().minusDays(50),
                tom = LocalDate.now()
            )
        )
        assertEquals(true, søknadprosess.resultat())
    }

    @Test
    fun `Faktumrekkefølge i seksjon`() {
        val søknad = Søknad(Prosessversjon(Prosess.Dagpenger, -1), *EøsArbeidsforhold.fakta())
        val søknadprosess = søknad.testSøknadprosess(
            EøsArbeidsforhold.regeltre(søknad)
        ) {
            EøsArbeidsforhold.seksjon(this)
        }
        val faktaFraEøsArbeidsforhold = søknadprosess.nesteSeksjoner().first().joinToString(separator = ",") { it.id }
        assertEquals("9001,9002,9003,9004,9005,9006", faktaFraEøsArbeidsforhold)
    }
}
