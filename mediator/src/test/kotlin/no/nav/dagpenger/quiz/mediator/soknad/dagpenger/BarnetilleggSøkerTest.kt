package no.nav.dagpenger.quiz.mediator.soknad.dagpenger

import no.nav.dagpenger.model.faktum.Land
import no.nav.dagpenger.model.faktum.Prosessversjon
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.faktum.Tekst
import no.nav.dagpenger.model.seksjon.Søknadprosess
import no.nav.dagpenger.quiz.mediator.helpers.testSøknadprosess
import no.nav.dagpenger.quiz.mediator.soknad.Prosess
import no.nav.dagpenger.quiz.mediator.soknad.verifiserFeltsammensetting
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDate

internal class BarnetilleggSøkerTest {
    private val søknad = Søknad(Prosessversjon(Prosess.Dagpenger, -1), *BarnetilleggSøker.fakta())
    private lateinit var søknadprosess: Søknadprosess

    @Test
    fun `Sjekk om faktasammensettingen har endret seg siden sist`() {
        BarnetilleggSøker.verifiserFeltsammensetting(8, 8036)
    }

    @BeforeEach
    fun setup() {
        søknadprosess = søknad.testSøknadprosess(
            BarnetilleggSøker.regeltre(søknad)
        )
    }

    @Test
    fun `Må ikke besvare noe om vi ikke finner noen barn i registeret`() {
        søknadprosess.generator(BarnetilleggSøker.`barn liste`).besvar(0)
        assertEquals(true, søknadprosess.resultat())
    }

    @Test
    fun `Regeltre BarnetilleggSøker med 1 barn`() {
        søknadprosess.generator(BarnetilleggSøker.`barn liste`).besvar(1)
        assertEquals(null, søknadprosess.resultat())

        søknadprosess.tekst("${BarnetilleggSøker.`barn fornavn mellomnavn`}.1").besvar(Tekst("Ola"))
        søknadprosess.tekst("${BarnetilleggSøker.`barn etternavn`}.1").besvar(Tekst("Nordmann"))
        søknadprosess.dato("${BarnetilleggSøker.`barn foedselsdato`}.1").besvar(LocalDate.now().minusYears(1))
        søknadprosess.land("${BarnetilleggSøker.`barn statsborgerskap`}.1").besvar(Land("NOR"))
        søknadprosess.boolsk("${BarnetilleggSøker.`forsoerger du barnet`}.1").besvar(false)
        assertEquals(true, søknadprosess.resultat())

        søknadprosess.boolsk("${BarnetilleggSøker.`forsoerger du barnet`}.1").besvar(true)
        assertEquals(null, søknadprosess.resultat())

        søknadprosess.boolsk("${BarnetilleggSøker.`barn aarsinntekt over 1g`}.1").besvar(true)
        søknadprosess.heltall("${BarnetilleggSøker.`barn inntekt`}.1").besvar(100)
        assertEquals(true, søknadprosess.resultat())
    }
}
