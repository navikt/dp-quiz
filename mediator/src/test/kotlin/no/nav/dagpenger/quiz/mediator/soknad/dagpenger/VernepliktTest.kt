package no.nav.dagpenger.quiz.mediator.soknad.dagpenger

import no.nav.dagpenger.model.faktum.Prosessversjon
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.seksjon.Søknadprosess
import no.nav.dagpenger.quiz.mediator.helpers.testSøknadprosess
import no.nav.dagpenger.quiz.mediator.soknad.Prosess
import no.nav.dagpenger.quiz.mediator.soknad.verifiserFeltsammensetting
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class VernepliktTest {

    private val søknad = Søknad(Prosessversjon(Prosess.Dagpenger, -1), *Verneplikt.fakta())
    private lateinit var søknadprosess: Søknadprosess

    @Test
    fun `Sjekk om faktasammensettingen har endret seg siden sist`() {
        Verneplikt.verifiserFeltsammensetting(1, 7001)
    }

    @BeforeEach
    fun setup() {
        søknadprosess = søknad.testSøknadprosess(
            Verneplikt.regeltre(søknad)
        )
    }

    @Test
    fun `Har ikke avtjent verneplikt`() {
        søknadprosess.boolsk(Verneplikt.`avtjent militaer sivilforsvar tjeneste siste 12 mnd`).besvar(false)
        assertEquals(true, søknadprosess.resultat())
    }

    @Test
    fun `Har avtjent verneplikt`() {
        søknadprosess.boolsk(Verneplikt.`avtjent militaer sivilforsvar tjeneste siste 12 mnd`).besvar(true)
        assertEquals(true, søknadprosess.resultat())
    }
}
