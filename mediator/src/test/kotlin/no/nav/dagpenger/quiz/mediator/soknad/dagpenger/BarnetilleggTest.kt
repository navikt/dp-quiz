package no.nav.dagpenger.quiz.mediator.soknad.dagpenger

import no.nav.dagpenger.model.faktum.Land
import no.nav.dagpenger.model.faktum.Prosessversjon
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.faktum.Tekst
import no.nav.dagpenger.quiz.mediator.helpers.testSøknadprosess
import no.nav.dagpenger.quiz.mediator.soknad.Prosess
import no.nav.dagpenger.quiz.mediator.soknad.verifiserFeltsammensetting
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDate

internal class BarnetilleggTest {

    @Test
    fun `Sjekk om faktasammensettingen har endret seg siden sist`() {
        Barnetillegg.verifiserFeltsammensetting(8, 8036)
    }

    @Test
    fun `Regeltre barnetillegg med 1 barn`() {
        val søknad = Søknad(Prosessversjon(Prosess.Dagpenger, -1), *Barnetillegg.fakta())
        val søknadprosess = søknad.testSøknadprosess(
            Barnetillegg.regeltre(søknad)
        )

        søknadprosess.generator(Barnetillegg.`barn liste`).besvar(0)
        assertEquals(true, søknadprosess.resultat())

        søknadprosess.generator(Barnetillegg.`barn liste`).besvar(1)
        assertEquals(null, søknadprosess.resultat())

        søknadprosess.tekst("${Barnetillegg.`barn fornavn mellomnavn`}.1").besvar(Tekst("Ola"))
        søknadprosess.tekst("${Barnetillegg.`barn etternavn`}.1").besvar(Tekst("Nordmann"))
        søknadprosess.dato("${Barnetillegg.`barn foedselsdato`}.1").besvar(LocalDate.now().minusYears(1))
        søknadprosess.land("${Barnetillegg.`barn bostedsland`}.1").besvar(Land("NOR"))
        søknadprosess.boolsk("${Barnetillegg.`forsoerger du barnet`}.1").besvar(false)
        assertEquals(true, søknadprosess.resultat())

        søknadprosess.boolsk("${Barnetillegg.`forsoerger du barnet`}.1").besvar(true)
        assertEquals(null, søknadprosess.resultat())

        søknadprosess.boolsk("${Barnetillegg.`barn aarsinntekt over 1g`}.1").besvar(true)
        søknadprosess.heltall("${Barnetillegg.`barn inntekt`}.1").besvar(100)
        assertEquals(true, søknadprosess.resultat())
    }
}
