package no.nav.dagpenger.quiz.mediator.soknad.dagpenger

import no.nav.dagpenger.model.faktum.Prosessversjon
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.seksjon.Faktagrupper
import no.nav.dagpenger.quiz.mediator.helpers.testSøknadprosess
import no.nav.dagpenger.quiz.mediator.soknad.Prosess
import no.nav.dagpenger.quiz.mediator.soknad.verifiserFeltsammensetting
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class VernepliktTest {

    private val søknad = Søknad(Prosessversjon(Prosess.Dagpenger, -1), *Verneplikt.fakta())
    private lateinit var faktagrupper: Faktagrupper

    @Test
    fun `Sjekk om faktasammensettingen har endret seg siden sist`() {
        Verneplikt.verifiserFeltsammensetting(3, 21006)
    }

    @BeforeEach
    fun setup() {
        faktagrupper = søknad.testSøknadprosess(
            Verneplikt.regeltre(søknad)
        ) {
            Verneplikt.seksjon(this)
        }
    }

    @Test
    fun `Har ikke avtjent verneplikt`() {
        faktagrupper.boolsk(Verneplikt.`avtjent militær sivilforsvar tjeneste siste 12 mnd`).besvar(false)
        assertEquals(true, faktagrupper.resultat())
    }

    @Test
    fun `Har avtjent verneplikt`() {
        faktagrupper.boolsk(Verneplikt.`avtjent militær sivilforsvar tjeneste siste 12 mnd`).besvar(true)
        assertEquals(true, faktagrupper.resultat())
    }

    @Test
    fun `Faktarekkefølge i seksjon`() {
        val faktaFraVerneplikt = faktagrupper.nesteSeksjoner().first().joinToString(separator = ",") { it.id }
        assertEquals("7001,7002,7003", faktaFraVerneplikt)
    }
}
