package no.nav.dagpenger.quiz.mediator.soknad.dagpenger

import no.nav.dagpenger.model.faktum.Prosessversjon
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.seksjon.Søknadprosess
import no.nav.dagpenger.quiz.mediator.helpers.testSøknadprosess
import no.nav.dagpenger.quiz.mediator.soknad.Prosess
import no.nav.dagpenger.quiz.mediator.soknad.verifiserFeltsammensetting
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class BarnetilleggRegisterTest {

    private val søknad = Søknad(Prosessversjon(Prosess.Dagpenger, -1), *BarnetilleggRegister.fakta())
    private lateinit var søknadprosess: Søknadprosess

    @BeforeEach
    fun setup() {
        søknadprosess = søknad.testSøknadprosess(
            BarnetilleggRegister.regeltre(søknad)
        )
    }

    @Test
    fun `Sjekk om faktasammensettingen har endret seg siden sist`() {
        BarnetilleggRegister.verifiserFeltsammensetting(8, 8100)
    }

    @Test
    fun `Må ikke besvare noe om vi ikke finner noen barn i registeret`() {
        søknadprosess.generator(BarnetilleggRegister.`barn liste register`).besvar(0)
        assertEquals(true, søknadprosess.resultat())
    }
}
