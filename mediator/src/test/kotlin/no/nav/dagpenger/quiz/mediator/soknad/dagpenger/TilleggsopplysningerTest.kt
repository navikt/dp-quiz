package no.nav.dagpenger.quiz.mediator.soknad.dagpenger

import no.nav.dagpenger.model.faktum.Fakta
import no.nav.dagpenger.model.faktum.Faktaversjon
import no.nav.dagpenger.model.faktum.Faktum
import no.nav.dagpenger.model.faktum.Tekst
import no.nav.dagpenger.model.seksjon.Prosess
import no.nav.dagpenger.quiz.mediator.helpers.testSøknadprosess
import no.nav.dagpenger.quiz.mediator.soknad.Prosessfakta
import no.nav.dagpenger.quiz.mediator.soknad.verifiserFeltsammensetting
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class TilleggsopplysningerTest {

    private val fakta = Fakta(Faktaversjon(Prosessfakta.Dagpenger, -1), *Tilleggsopplysninger.fakta())
    private lateinit var prosess: Prosess
    private lateinit var harTilleggsopplysninger: Faktum<Boolean>
    private lateinit var tilleggsopplysninger: Faktum<Tekst>

    @Test
    fun `Sjekk om faktasammensettingen har endret seg siden sist`() {
        Tilleggsopplysninger.verifiserFeltsammensetting(2, 8003)
    }

    @BeforeEach
    fun setup() {
        prosess = fakta.testSøknadprosess(Tilleggsopplysninger.regeltre(fakta)) {
            Tilleggsopplysninger.seksjon(this)
        }
        harTilleggsopplysninger = prosess.boolsk(Tilleggsopplysninger.`har tilleggsopplysninger`)
        tilleggsopplysninger = prosess.tekst(Tilleggsopplysninger.tilleggsopplysninger)
    }

    @Test
    fun `Har tilleggsopplysninger eller ikke`() {
        harTilleggsopplysninger.besvar(false)
        assertEquals(true, prosess.resultat())

        harTilleggsopplysninger.besvar(true)
        assertEquals(null, prosess.resultat())

        tilleggsopplysninger.besvar(Tekst("Tilleggsopplysninger"))
        assertEquals(true, prosess.resultat())
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
        val faktarekkefølge = prosess.nesteSeksjoner().first().joinToString(separator = ",") { it.id }
        assertEquals("4002,4001", faktarekkefølge)
    }
}
