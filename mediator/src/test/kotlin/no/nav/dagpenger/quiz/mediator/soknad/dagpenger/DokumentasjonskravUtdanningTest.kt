package no.nav.dagpenger.quiz.mediator.soknad.dagpenger

import no.nav.dagpenger.model.faktum.Dokument
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

internal class DokumentasjonskravUtdanningTest {
    private val dokumentfakta = Utdanning.fakta() + DokumentasjonskravUtdanning.fakta()
    private val søknad = Søknad(Prosessversjon(Prosess.Dagpenger, -1), *dokumentfakta)
    private lateinit var søknadprosess: Søknadprosess

    @Test
    fun `Sjekk om faktasammensettingen har endret seg siden sist`() {
        DokumentasjonskravUtdanning.verifiserFeltsammensetting(3, 36006)
    }

    @BeforeEach
    fun setup() {
        søknadprosess = søknad.testSøknadprosess(
            DokumentasjonskravUtdanning.regeltre(søknad)
        ) {
            DokumentasjonskravUtdanning.seksjon(this)
        }
    }

    @Test
    fun `Har ikke avsluttet utdanning i løpet av de siste seks månedene`() {
        søknadprosess.boolsk(Utdanning.`avsluttet utdanning siste 6 mnd`).besvar(false)
        assertEquals(true, søknadprosess.resultat())
    }

    @Test
    fun `Har avsluttet utdanning nylig og har dokumentasjon på sluttdato tilgjengelig`() {
        søknadprosess.boolsk(Utdanning.`avsluttet utdanning siste 6 mnd`).besvar(true)
        søknadprosess.boolsk(DokumentasjonskravUtdanning.`dokumentasjon på sluttdato tilgjengelig`).besvar(true)
        søknadprosess.dokument(DokumentasjonskravUtdanning.`dokumentasjon på sluttdato`).besvar(Dokument(lastOppTidsstempel = LocalDate.now(), "urn:nav:vedlegg:1"))
        assertEquals(true, søknadprosess.resultat())
    }

    @Test
    fun `Har avsluttet utdanning nylig men har ikke dokumentasjon på sluttdato tilgjengelig`() {
        søknadprosess.boolsk(Utdanning.`avsluttet utdanning siste 6 mnd`).besvar(true)
        søknadprosess.boolsk(DokumentasjonskravUtdanning.`dokumentasjon på sluttdato tilgjengelig`).besvar(false)
        søknadprosess.tekst(DokumentasjonskravUtdanning.`dokumentasjon på sluttdato ikke tilgjengelig årsak`).besvar(Tekst("Årsak"))
        assertEquals(true, søknadprosess.resultat())
    }
}
