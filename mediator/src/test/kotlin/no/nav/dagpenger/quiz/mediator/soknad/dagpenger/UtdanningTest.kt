package no.nav.dagpenger.quiz.mediator.soknad.dagpenger

import no.nav.dagpenger.model.faktum.Dokument
import no.nav.dagpenger.model.faktum.Faktum
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
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class UtdanningTest {

    private val søknad = Søknad(Prosessversjon(Prosess.Dagpenger, -1), *Utdanning.fakta())
    private lateinit var søknadprosess: Søknadprosess
    private lateinit var tarUtdanning: Faktum<Boolean>
    private lateinit var nyligAvsluttetUtdanning: Faktum<Boolean>
    private lateinit var planleggerUtdanning: Faktum<Boolean>

    @Test
    fun `Sjekk om faktasammensettingen har endret seg siden sist`() {
        Utdanning.verifiserFeltsammensetting(5, 10015)
    }

    @BeforeEach
    fun setup() {
        søknadprosess = søknad.testSøknadprosess(Utdanning.regeltre(søknad)) {
            Utdanning.seksjon(this)
        }

        tarUtdanning = søknadprosess.boolsk(Utdanning.`tar du utdanning`)
        nyligAvsluttetUtdanning = søknadprosess.boolsk(Utdanning.`avsluttet utdanning siste 6 mnd`)
        planleggerUtdanning = søknadprosess.boolsk(Utdanning.`planlegger utdanning med dagpenger`)
    }

    @Test
    fun `Tar utdanning`() {
        tarUtdanning.besvar(true)
        assertEquals(true, søknadprosess.resultat())
    }

    @Test
    fun `Ingen utdanning`() {
        tarUtdanning.besvar(false)
        assertEquals(null, søknadprosess.resultat())

        nyligAvsluttetUtdanning.besvar(true)
        planleggerUtdanning.besvar(true)
        assertEquals(true, søknadprosess.resultat())
    }

    @Test
    fun avhengigheter() {
        tarUtdanning.besvar(false)
        nyligAvsluttetUtdanning.besvar(true)
        planleggerUtdanning.besvar(true)
        erBesvart(nyligAvsluttetUtdanning, planleggerUtdanning)

        tarUtdanning.besvar(true)
        erUbesvart(nyligAvsluttetUtdanning, planleggerUtdanning)
    }

    @Test
    fun `Faktarekkefølge i seksjon`() {
        val faktaFraUtdanning = søknadprosess.nesteSeksjoner().first().joinToString(separator = ",") { it.id }
        assertEquals("2001,2002,2003,2004", faktaFraUtdanning)
    }

    @Test
    fun `Godkjenningsseksjon fungerer`() {
        tarUtdanning.besvar(true)
        nyligAvsluttetUtdanning.besvar(true)
        planleggerUtdanning.besvar(true)
        søknadprosess.dokument(Utdanning.`dokumentasjon på sluttdato`).besvar(Dokument(LocalDate.now(), "urn:test:test"))
        assertEquals("godkjenning dokumentasjon utdanning", søknadprosess.nesteSeksjoner().first().navn)
    }

    private fun erBesvart(vararg fakta: Faktum<*>) =
        fakta.forEach { faktum ->
            assertTrue(faktum.erBesvart())
        }

    private fun erUbesvart(vararg fakta: Faktum<*>) =
        fakta.forEach { faktum ->
            assertFalse(faktum.erBesvart())
        }
}
