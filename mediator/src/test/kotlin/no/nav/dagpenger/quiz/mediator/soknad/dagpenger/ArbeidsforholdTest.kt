package no.nav.dagpenger.quiz.mediator.soknad.dagpenger

import no.nav.dagpenger.model.faktum.Envalg
import no.nav.dagpenger.model.faktum.Land
import no.nav.dagpenger.model.faktum.Periode
import no.nav.dagpenger.model.faktum.Prosessversjon
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.faktum.Tekst
import no.nav.dagpenger.model.seksjon.Søknadprosess
import no.nav.dagpenger.quiz.mediator.helpers.februar
import no.nav.dagpenger.quiz.mediator.helpers.januar
import no.nav.dagpenger.quiz.mediator.helpers.testSøknadprosess
import no.nav.dagpenger.quiz.mediator.soknad.Prosess
import no.nav.dagpenger.quiz.mediator.soknad.verifiserFeltsammensetting
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class ArbeidsforholdTest {
    private val søknad = Søknad(Prosessversjon(Prosess.Dagpenger, -1), *Arbeidsforhold.fakta())
    private lateinit var søknadprosess: Søknadprosess

    @Test
    fun `Sjekk om faktasammensettingen har endret seg siden sist`() {
        Arbeidsforhold.verifiserFeltsammensetting(47, 377128)
    }

    @BeforeEach
    fun setup() {
        søknadprosess = søknad.testSøknadprosess(Arbeidsforhold.regeltre(søknad))
    }

    @Test
    fun `ikke endret arbeidsforhold`() {
        `besvar innledende info om arbeidsforhold`()

        søknadprosess.envalg("${Arbeidsforhold.`arbeidsforhold endret`}.1")
            .besvar(Envalg("faktum.arbeidsforhold.endret.svar.ikke-endret"))
        søknadprosess.boolsk("${Arbeidsforhold.`arbeidsforhold kjent antall timer jobbet`}.1").besvar(true)
        søknadprosess.desimaltall("${Arbeidsforhold.`arbeidsforhold antall timer jobbet`}.1").besvar(40.5)
        søknadprosess.tekst("${Arbeidsforhold.`arbeidsforhold tilleggsopplysninger`}.1")
            .besvar(Tekst("Tilleggsopplysninger"))
        assertEquals(true, søknadprosess.resultat())
    }

    @Test
    fun avskjediget() {
        `besvar innledende info om arbeidsforhold`()

        søknadprosess.envalg("${Arbeidsforhold.`arbeidsforhold endret`}.1")
            .besvar(Envalg("faktum.arbeidsforhold.endret.svar.avskjediget"))
        søknadprosess.boolsk("${Arbeidsforhold.`arbeidsforhold vet du antall timer foer mistet jobb`}.1").besvar(true)
        søknadprosess.desimaltall("${Arbeidsforhold.`arbeidsforhold antall timer dette arbeidsforhold`}.1").besvar(40.5)
        søknadprosess.tekst("${Arbeidsforhold.`arbeidsforhold hva er aarsak til avskjediget`}.1").besvar(Tekst("Årsak"))
        assertEquals(true, søknadprosess.resultat())
    }

    @Test
    fun `sagt opp av arbeidsgiver`() {
        `besvar innledende info om arbeidsforhold`()

        søknadprosess.envalg("${Arbeidsforhold.`arbeidsforhold endret`}.1")
            .besvar(Envalg("faktum.arbeidsforhold.endret.svar.sagt-opp-av-arbeidsgiver"))

        søknadprosess.periode("${Arbeidsforhold.`arbeidsforhold varighet`}.1").besvar(Periode(1.januar, 1.februar))
        søknadprosess.boolsk("${Arbeidsforhold.`arbeidsforhold vet du antall timer foer mistet jobb`}.1").besvar(true)
        søknadprosess.desimaltall("${Arbeidsforhold.`arbeidsforhold antall timer dette arbeidsforhold`}.1").besvar(40.5)
        søknadprosess.tekst("${Arbeidsforhold.`arbeidsforhold vet du aarsak til sagt opp av arbeidsgiver`}.1").besvar(Tekst("Årsak"))
        søknadprosess.boolsk("${Arbeidsforhold.`arbeidsforhold tilbud om annen stilling eller annet sted i norge`}.1").besvar(true)
        søknadprosess.boolsk("${Arbeidsforhold.`arbeidsforhold skift eller turnus`}.1").besvar(true)

        søknadprosess.boolsk("${Arbeidsforhold.`arbeidsforhold rotasjon`}.1").besvar(false)
        assertEquals(true, søknadprosess.resultat())

        søknadprosess.boolsk("${Arbeidsforhold.`arbeidsforhold rotasjon`}.1").besvar(true)
        assertEquals(null, søknadprosess.resultat())

        søknadprosess.heltall("${Arbeidsforhold.`arbeidsforhold arbeidsdager siste rotasjon`}.1").besvar(20)
        søknadprosess.heltall("${Arbeidsforhold.`arbeidsforhold fridager siste rotasjon`}.1").besvar(2)
        assertEquals(true, søknadprosess.resultat())
    }

    private fun `besvar innledende info om arbeidsforhold`() {
        søknadprosess.dato(Arbeidsforhold.`dagpenger soknadsdato`).besvar(1.januar)
        søknadprosess.envalg(Arbeidsforhold.`type arbeidstid`).besvar(Envalg("faktum.type-arbeidstid.svar.fast"))
        søknadprosess.generator(Arbeidsforhold.arbeidsforhold).besvar(1)
        søknadprosess.tekst("${Arbeidsforhold.`arbeidsforhold navn bedrift`}.1").besvar(Tekst("Ullfabrikken"))
        søknadprosess.land("${Arbeidsforhold.`arbeidsforhold land`}.1").besvar(Land("NOR"))
    }
}
