package no.nav.dagpenger.quiz.mediator.soknad.dagpenger

import no.nav.dagpenger.model.faktum.Envalg
import no.nav.dagpenger.model.faktum.Land
import no.nav.dagpenger.model.faktum.Prosessversjon
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.faktum.Tekst
import no.nav.dagpenger.model.seksjon.Søknadprosess
import no.nav.dagpenger.quiz.mediator.helpers.januar
import no.nav.dagpenger.quiz.mediator.helpers.testSøknadprosess
import no.nav.dagpenger.quiz.mediator.soknad.Prosess
import no.nav.dagpenger.quiz.mediator.soknad.verifiserFeltsammensetting
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class ArbeidsforholdTest {
    private val søknad = Søknad(Prosessversjon(Prosess.Dagpenger, -1), *Arbeidsforhold.fakta())
    private lateinit var søknadprosess: Søknadprosess

    @Test
    fun `Sjekk om faktasammensettingen har endret seg siden sist`() {
        Arbeidsforhold.verifiserFeltsammensetting(47, 377128)
    }

    @BeforeEach
    fun setup(){
        søknadprosess = søknad.testSøknadprosess(Arbeidsforhold.regeltre(søknad))
    }

    @Test
    fun `Ikke endret arbeidsforhold arbeidsforhold`(){
        besvarInnledendeInfoOmArbeidsforhold()
        søknadprosess.envalg("${Arbeidsforhold.`arbeidsforhold endret`}.1").besvar(Envalg("faktum.arbeidsforhold.endret.svar.ikke-endret"))
        søknadprosess.boolsk("${Arbeidsforhold.`arbeidsforhold kjent antall timer jobbet`}.1").besvar(false)
        //søknadprosess.desimaltall("${Arbeidsforhold.`arbeidsforhold antall timer jobbet`}.1").besvar(40.5)
        søknadprosess.nesteSeksjoner()
    }

    private fun besvarInnledendeInfoOmArbeidsforhold() {
        søknadprosess.dato(Arbeidsforhold.`dagpenger soknadsdato`).besvar(1.januar)
        søknadprosess.envalg(Arbeidsforhold.`type arbeidstid`).besvar(Envalg("faktum.type-arbeidstid.svar.fast"))
        søknadprosess.generator(Arbeidsforhold.arbeidsforhold).besvar(1)
        søknadprosess.tekst("${Arbeidsforhold.`arbeidsforhold navn bedrift`}.1").besvar(Tekst("Ullfabrikken"))
        søknadprosess.land("${Arbeidsforhold.`arbeidsforhold land`}.1").besvar(Land("NOR"))
    }
}
