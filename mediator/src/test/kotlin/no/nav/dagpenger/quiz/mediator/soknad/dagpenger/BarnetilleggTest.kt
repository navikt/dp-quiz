package no.nav.dagpenger.quiz.mediator.soknad.dagpenger

import no.nav.dagpenger.model.faktum.Land
import no.nav.dagpenger.model.faktum.Prosessversjon
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.faktum.Tekst
import no.nav.dagpenger.quiz.mediator.helpers.testSøknadprosess
import no.nav.dagpenger.quiz.mediator.soknad.Prosess
import no.nav.dagpenger.quiz.mediator.soknad.verifiserFeltsammensetting
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDate


internal class BarnetilleggTest {

    @Test
    fun `Sjekk om faktasammensettingen har endret seg siden sist`() {
        Barnetillegg.verifiserFeltsammensetting(8, 8036)
    }

    @Test
    fun `Regeltre barnetillegg`(){
        val søknad = Søknad(Prosessversjon(Prosess.Dagpenger, -1), *Barnetillegg.fakta())
        val søknadprosess = søknad.testSøknadprosess(
            Barnetillegg.regeltre(søknad)
        )

        søknadprosess.generator(Barnetillegg.`barn liste`).besvar(0)
        assertEquals(false, søknadprosess.resultat()) //Denne skal egentlig bli true

        søknadprosess.generator(Barnetillegg.`barn liste`).besvar(1)
        assertEquals(null, søknadprosess.resultat())


        søknadprosess.tekst(Barnetillegg.`barn fornavn mellomnavn`).besvar(Tekst("Ola"))
        søknadprosess.tekst(Barnetillegg.`barn etternavn`).besvar(Tekst("Nordmann"))
        søknadprosess.dato(Barnetillegg.`barn foedselsdato`).besvar(LocalDate.now().minusYears(1))
        søknadprosess.land(Barnetillegg.`barn bostedsland`).besvar(Land("NOR"))
        søknadprosess.boolsk(Barnetillegg.`forsoerger du barnet`).besvar(true)
        //assertEquals(true, søknadprosess.resultat())   Denne skal være true dersom spm over besvares med false

        søknadprosess.boolsk(Barnetillegg.`forsoerger du barnet`).besvar(true)
        søknadprosess.boolsk(Barnetillegg.`barn aarsinntekt over 1g`).besvar(true)
        søknadprosess.heltall(Barnetillegg.`barn inntekt`).besvar(100)
        assertEquals(true, søknadprosess.resultat()) //Hvorfor blir denne null

    }
}
