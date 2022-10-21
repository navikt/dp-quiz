package no.nav.dagpenger.quiz.mediator.soknad.dagpenger

import no.nav.dagpenger.model.faktum.Dokument
import no.nav.dagpenger.model.faktum.Prosessversjon
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.seksjon.Søknadprosess
import no.nav.dagpenger.quiz.mediator.helpers.testSøknadprosess
import no.nav.dagpenger.quiz.mediator.soknad.Prosess
import no.nav.dagpenger.quiz.mediator.soknad.verifiserFeltsammensetting
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDate
import kotlin.test.assertEquals

internal class VernepliktTest {

    private val søknad = Søknad(Prosessversjon(Prosess.Dagpenger, -1), *Verneplikt.fakta())
    private lateinit var søknadprosess: Søknadprosess

    @Test
    fun `Sjekk om faktasammensettingen har endret seg siden sist`() {
        Verneplikt.verifiserFeltsammensetting(3, 21006)
    }

    @BeforeEach
    fun setup() {
        søknadprosess = søknad.testSøknadprosess(
            Verneplikt.regeltre(søknad)
        ) {
            Verneplikt.seksjon(this)
        }
    }

    @Test
    fun `Har ikke avtjent verneplikt`() {
        søknadprosess.boolsk(Verneplikt.`avtjent militær sivilforsvar tjeneste siste 12 mnd`).besvar(false)
        assertEquals(true, søknadprosess.resultat())
    }

    @Test
    fun `Har avtjent verneplikt`() {
        søknadprosess.boolsk(Verneplikt.`avtjent militær sivilforsvar tjeneste siste 12 mnd`).besvar(true)
        assertEquals(true, søknadprosess.resultat())
    }

    @Test
    fun `Godkjenning av dokumentasjon`() {
        søknadprosess.boolsk(Verneplikt.`avtjent militær sivilforsvar tjeneste siste 12 mnd`).besvar(true)
        assertEquals(true, søknadprosess.resultat())
        søknadprosess.dokument(Verneplikt.`avtjent militær sivilforsvar tjeneste siste 12 mnd dokumentasjon`).besvar(
            Dokument(LocalDate.now(), "urn:test:test")
        )
        assertEquals("godkjenning dokumentasjon verneplikt", søknadprosess.nesteSeksjoner().first().navn)
    }

    @Test
    fun `Faktarekkefølge i seksjon`() {
        val faktaFraVerneplikt = søknadprosess.nesteSeksjoner().first().joinToString(separator = ",") { it.id }
        assertEquals("7001,7002", faktaFraVerneplikt)
    }
}
