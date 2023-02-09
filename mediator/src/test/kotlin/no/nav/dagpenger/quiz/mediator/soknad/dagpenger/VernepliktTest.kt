package no.nav.dagpenger.quiz.mediator.soknad.dagpenger

import no.nav.dagpenger.model.faktum.Fakta
import no.nav.dagpenger.model.faktum.Faktaversjon
import no.nav.dagpenger.model.seksjon.Utredningsprosess
import no.nav.dagpenger.quiz.mediator.helpers.testSøknadprosess
import no.nav.dagpenger.quiz.mediator.soknad.Prosessfakta
import no.nav.dagpenger.quiz.mediator.soknad.verifiserFeltsammensetting
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class VernepliktTest {

    private val fakta = Fakta(Faktaversjon(Prosessfakta.Dagpenger, -1), *Verneplikt.fakta())
    private lateinit var utredningsprosess: Utredningsprosess

    @Test
    fun `Sjekk om faktasammensettingen har endret seg siden sist`() {
        Verneplikt.verifiserFeltsammensetting(3, 21006)
    }

    @BeforeEach
    fun setup() {
        utredningsprosess = fakta.testSøknadprosess(
            Verneplikt.regeltre(fakta),
        ) {
            Verneplikt.seksjon(this)
        }
    }

    @Test
    fun `Har ikke avtjent verneplikt`() {
        utredningsprosess.boolsk(Verneplikt.`avtjent militær sivilforsvar tjeneste siste 12 mnd`).besvar(false)
        assertEquals(true, utredningsprosess.resultat())
    }

    @Test
    fun `Har avtjent verneplikt`() {
        utredningsprosess.boolsk(Verneplikt.`avtjent militær sivilforsvar tjeneste siste 12 mnd`).besvar(true)
        assertEquals(true, utredningsprosess.resultat())
    }

    @Test
    fun `Faktarekkefølge i seksjon`() {
        val faktaFraVerneplikt = utredningsprosess.nesteSeksjoner().first().joinToString(separator = ",") { it.id }
        assertEquals("7001,7002,7003", faktaFraVerneplikt)
    }
}
