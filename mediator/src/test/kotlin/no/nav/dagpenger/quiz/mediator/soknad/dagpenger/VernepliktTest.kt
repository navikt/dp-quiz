package no.nav.dagpenger.quiz.mediator.soknad.dagpenger

import no.nav.dagpenger.model.faktum.Fakta
import no.nav.dagpenger.model.seksjon.Prosess
import no.nav.dagpenger.quiz.mediator.helpers.testFaktaversjon
import no.nav.dagpenger.quiz.mediator.helpers.testSøknadprosess
import no.nav.dagpenger.quiz.mediator.soknad.verifiserFeltsammensetting
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class VernepliktTest {

    private val fakta = Fakta(testFaktaversjon(), *Verneplikt.fakta())
    private lateinit var prosess: Prosess

    @Test
    fun `Sjekk om faktasammensettingen har endret seg siden sist`() {
        Verneplikt.verifiserFeltsammensetting(3, 21006)
    }

    @BeforeEach
    fun setup() {
        prosess = fakta.testSøknadprosess(
            subsumsjon = Verneplikt.regeltre(fakta),
        ) {
            Verneplikt.seksjon(this)
        }
    }

    @Test
    fun `Har ikke avtjent verneplikt`() {
        prosess.boolsk(Verneplikt.`avtjent militær sivilforsvar tjeneste siste 12 mnd`).besvar(false)
        assertEquals(true, prosess.resultat())
    }

    @Test
    fun `Har avtjent verneplikt`() {
        prosess.boolsk(Verneplikt.`avtjent militær sivilforsvar tjeneste siste 12 mnd`).besvar(true)
        assertEquals(true, prosess.resultat())
    }

    @Test
    fun `Faktarekkefølge i seksjon`() {
        val faktaFraVerneplikt = prosess.nesteSeksjoner().first().joinToString(separator = ",") { it.id }
        assertEquals("7001,7002,7003", faktaFraVerneplikt)
    }
}
