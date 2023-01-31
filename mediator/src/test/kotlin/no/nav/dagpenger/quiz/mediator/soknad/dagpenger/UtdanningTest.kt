package no.nav.dagpenger.quiz.mediator.soknad.dagpenger

import no.nav.dagpenger.model.faktum.Faktum
import no.nav.dagpenger.model.faktum.Prosessversjon
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.seksjon.Faktagrupper
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
    private lateinit var faktagrupper: Faktagrupper
    private lateinit var tarUtdanning: Faktum<Boolean>
    private lateinit var nyligAvsluttetUtdanning: Faktum<Boolean>
    private lateinit var planleggerUtdanning: Faktum<Boolean>

    @Test
    fun `Sjekk om faktasammensettingen har endret seg siden sist`() {
        Utdanning.verifiserFeltsammensetting(5, 10015)
    }

    @BeforeEach
    fun setup() {
        faktagrupper = søknad.testSøknadprosess(Utdanning.regeltre(søknad)) {
            Utdanning.seksjon(this)
        }

        tarUtdanning = faktagrupper.boolsk(Utdanning.`tar du utdanning`)
        nyligAvsluttetUtdanning = faktagrupper.boolsk(Utdanning.`avsluttet utdanning siste 6 mnd`)
        planleggerUtdanning = faktagrupper.boolsk(Utdanning.`planlegger utdanning med dagpenger`)
    }

    @Test
    fun `Tar utdanning`() {
        tarUtdanning.besvar(true)
        assertEquals(true, faktagrupper.resultat())
    }

    @Test
    fun `Ingen utdanning`() {
        tarUtdanning.besvar(false)
        assertEquals(null, faktagrupper.resultat())

        nyligAvsluttetUtdanning.besvar(true)
        planleggerUtdanning.besvar(true)
        assertEquals(true, faktagrupper.resultat())
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
        val faktaFraUtdanning = faktagrupper.nesteSeksjoner().first().joinToString(separator = ",") { it.id }
        assertEquals("2001,2002,2003,2004,2005", faktaFraUtdanning)
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
