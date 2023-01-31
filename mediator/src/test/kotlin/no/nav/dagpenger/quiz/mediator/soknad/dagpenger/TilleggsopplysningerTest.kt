package no.nav.dagpenger.quiz.mediator.soknad.dagpenger

import no.nav.dagpenger.model.faktum.Faktum
import no.nav.dagpenger.model.faktum.HenvendelsesType
import no.nav.dagpenger.model.faktum.Fakta
import no.nav.dagpenger.model.faktum.Tekst
import no.nav.dagpenger.model.seksjon.Faktagrupper
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
    private lateinit var faktagrupper: Faktagrupper
    private lateinit var harTilleggsopplysninger: Faktum<Boolean>
    private lateinit var tilleggsopplysninger: Faktum<Tekst>

    @Test
    fun `Sjekk om faktasammensettingen har endret seg siden sist`() {
        Tilleggsopplysninger.verifiserFeltsammensetting(2, 8003)
    }

    @BeforeEach
    fun setup() {
        faktagrupper = fakta.testSøknadprosess(Tilleggsopplysninger.regeltre(fakta)) {
            Tilleggsopplysninger.seksjon(this)
        }
        harTilleggsopplysninger = faktagrupper.boolsk(Tilleggsopplysninger.`har tilleggsopplysninger`)
        tilleggsopplysninger = faktagrupper.tekst(Tilleggsopplysninger.tilleggsopplysninger)
    }

    @Test
    fun `Har tilleggsopplysninger eller ikke`() {
        harTilleggsopplysninger.besvar(false)
        assertEquals(true, faktagrupper.resultat())

        harTilleggsopplysninger.besvar(true)
        assertEquals(null, faktagrupper.resultat())

        tilleggsopplysninger.besvar(Tekst("Tilleggsopplysninger"))
        assertEquals(true, faktagrupper.resultat())
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
        val faktarekkefølge = faktagrupper.nesteSeksjoner().first().joinToString(separator = ",") { it.id }
        assertEquals("4002,4001", faktarekkefølge)
    }
}
