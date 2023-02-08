package no.nav.dagpenger.quiz.mediator.soknad.dagpenger

import no.nav.dagpenger.model.faktum.Fakta
import no.nav.dagpenger.model.faktum.Faktum
import no.nav.dagpenger.model.faktum.HenvendelsesType
import no.nav.dagpenger.model.faktum.Tekst
import no.nav.dagpenger.model.seksjon.Utredningsprosess
import no.nav.dagpenger.quiz.mediator.helpers.testSøknadprosess
import no.nav.dagpenger.quiz.mediator.soknad.Prosess
import no.nav.dagpenger.quiz.mediator.soknad.verifiserFeltsammensetting
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class TilleggsopplysningerTest {

    private val fakta = Fakta(HenvendelsesType(Prosess.Dagpenger, -1), *Tilleggsopplysninger.fakta())
    private lateinit var utredningsprosess: Utredningsprosess
    private lateinit var harTilleggsopplysninger: Faktum<Boolean>
    private lateinit var tilleggsopplysninger: Faktum<Tekst>

    @Test
    fun `Sjekk om faktasammensettingen har endret seg siden sist`() {
        Tilleggsopplysninger.verifiserFeltsammensetting(2, 8003)
    }

    @BeforeEach
    fun setup() {
        utredningsprosess = fakta.testSøknadprosess(Tilleggsopplysninger.regeltre(fakta)) {
            Tilleggsopplysninger.seksjon(this)
        }
        harTilleggsopplysninger = utredningsprosess.boolsk(Tilleggsopplysninger.`har tilleggsopplysninger`)
        tilleggsopplysninger = utredningsprosess.tekst(Tilleggsopplysninger.tilleggsopplysninger)
    }

    @Test
    fun `Har tilleggsopplysninger eller ikke`() {
        harTilleggsopplysninger.besvar(false)
        assertEquals(true, utredningsprosess.resultat())

        harTilleggsopplysninger.besvar(true)
        assertEquals(null, utredningsprosess.resultat())

        tilleggsopplysninger.besvar(Tekst("Tilleggsopplysninger"))
        assertEquals(true, utredningsprosess.resultat())
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
        val faktarekkefølge = utredningsprosess.nesteSeksjoner().first().joinToString(separator = ",") { it.id }
        assertEquals("4002,4001", faktarekkefølge)
    }
}
