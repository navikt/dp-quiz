package no.nav.dagpenger.quiz.mediator.soknad.dagpenger

import no.nav.dagpenger.model.faktum.Faktum
import no.nav.dagpenger.model.faktum.Prosessversjon
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.seksjon.Søknadprosess
import no.nav.dagpenger.quiz.mediator.helpers.testSøknadprosess
import no.nav.dagpenger.quiz.mediator.soknad.Prosess
import no.nav.dagpenger.quiz.mediator.soknad.verifiserFeltsammensetting
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
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
        Utdanning.verifiserFeltsammensetting(3, 6006)
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

    private fun erBesvart(vararg fakta: Faktum<*>) =
        fakta.forEach { faktum ->
            assertTrue(faktum.erBesvart())
        }

    private fun erUbesvart(vararg fakta: Faktum<*>) =
        fakta.forEach { faktum ->
            assertFalse(faktum.erBesvart())
        }
}
