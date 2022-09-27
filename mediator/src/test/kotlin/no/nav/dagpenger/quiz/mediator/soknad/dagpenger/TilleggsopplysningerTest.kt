package no.nav.dagpenger.quiz.mediator.soknad.dagpenger

import no.nav.dagpenger.model.faktum.Faktum
import no.nav.dagpenger.model.faktum.Prosessversjon
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.faktum.Tekst
import no.nav.dagpenger.model.seksjon.Søknadprosess
import no.nav.dagpenger.quiz.mediator.helpers.testSøknadprosess
import no.nav.dagpenger.quiz.mediator.soknad.Prosess
import no.nav.dagpenger.quiz.mediator.soknad.verifiserFeltsammensetting
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class TilleggsopplysningerTest {

    private val søknad = Søknad(Prosessversjon(Prosess.Dagpenger, -1), *Tilleggsopplysninger.fakta())
    private lateinit var søknadprosess: Søknadprosess
    private lateinit var harTilleggsopplysninger: Faktum<Boolean>
    private lateinit var tilleggsopplysninger: Faktum<Tekst>

    @Test
    fun `Sjekk om faktasammensettingen har endret seg siden sist`() {
        Tilleggsopplysninger.verifiserFeltsammensetting(2, 8003)
    }

    @BeforeEach
    fun setup() {
        søknadprosess = søknad.testSøknadprosess(Tilleggsopplysninger.regeltre(søknad)) {
            Tilleggsopplysninger.seksjon(this)
        }
        harTilleggsopplysninger = søknadprosess.boolsk(Tilleggsopplysninger.`har tilleggsopplysninger`)
        tilleggsopplysninger = søknadprosess.tekst(Tilleggsopplysninger.tilleggsopplysninger)
    }

    @Test
    fun `Har tilleggsopplysninger eller ikke`() {
        harTilleggsopplysninger.besvar(false)
        assertEquals(true, søknadprosess.resultat())

        harTilleggsopplysninger.besvar(true)
        assertEquals(null, søknadprosess.resultat())

        tilleggsopplysninger.besvar(Tekst("Tilleggsopplysninger"))
        assertEquals(true, søknadprosess.resultat())
    }

    @Test
    fun avhengigheter() {
        harTilleggsopplysninger.besvar(true)
        tilleggsopplysninger.besvar(Tekst("Tilleggsopplysninger"))
        assertTrue(tilleggsopplysninger.erBesvart())

        harTilleggsopplysninger.besvar(false)
        assertFalse(tilleggsopplysninger.erBesvart())
    }

    @Test
    fun `Faktarekkefølge i seksjon`() {
        assertEquals("4002,4001", søknadprosess.nesteSeksjoner().first().joinToString(separator = ",") { it.id })
    }
}
