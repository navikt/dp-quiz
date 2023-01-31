package no.nav.dagpenger.model.unit.faktum

import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.dato
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.flervalg
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.heltall
import no.nav.dagpenger.model.faktum.Flervalg
import no.nav.dagpenger.model.faktum.Fakta
import no.nav.dagpenger.model.helpers.testSøknadprosess
import no.nav.dagpenger.model.helpers.testversjon
import no.nav.dagpenger.model.regel.inneholder
import no.nav.dagpenger.model.seksjon.Faktagrupper
import no.nav.dagpenger.model.subsumsjon.Subsumsjon
import no.nav.dagpenger.model.subsumsjon.TomSubsumsjon
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class FlervalgFaktumTest {

    val prototypeFakta = Fakta(
        testversjon,
        flervalg faktum "flervalg1" med "valg1" med "valg2" med "valg3" id 1,
        flervalg faktum "flervalg2" med "valg1" med "valg2" med "valg3" id 2,
        dato faktum "dato" id 3,
        heltall faktum "generator" id 4 genererer 2 og 3

    )

    lateinit var søknad: Faktagrupper

    @BeforeEach
    fun setup() {
        søknad = prototypeFakta.testSøknadprosess(TomSubsumsjon)
    }

    @Test
    fun `Skal kunne være lov å svare med gyldige valg`() {
        val flervalg = søknad.flervalg(1)
        assertDoesNotThrow { flervalg.besvar(Flervalg("flervalg1.valg1", "flervalg1.valg2")) }
        assertTrue(flervalg.erBesvart())
        assertEquals(Flervalg("flervalg1.valg1", "flervalg1.valg2"), flervalg.svar())
    }

    @Test
    fun `Skal ikke kunne opprette et tomt Valg`() {
        assertThrows<IllegalArgumentException> { Flervalg() }
        assertThrows<IllegalArgumentException> {
            Fakta(
                testversjon,
                flervalg faktum "flervalg" id 1
            ).testSøknadprosess(TomSubsumsjon)
        }
    }

    @Test
    fun `flervalg kan være template faktum`() {
        søknad.generator(4).besvar(1)
        val flervalg = søknad.flervalg("2.1")
        flervalg.besvar(Flervalg("flervalg2.valg3"))
        assertTrue(flervalg.erBesvart())
        assertEquals(Flervalg("flervalg2.valg3"), flervalg.svar())
    }

    @Test
    fun `Skal kaste feil hvis en svarer med ugyldige valg`() {
        val flervalg = søknad.flervalg(1)
        assertThrows<IllegalArgumentException> { flervalg.besvar(Flervalg("ugyldig-valg")) }
    }

    @Test
    fun `likhet test `() {
        val flervalg = Flervalg("flervalg1.valg1", "flervalg1.valg2")
        assertEquals(flervalg, flervalg)
        assertEquals(flervalg, Flervalg("flervalg1.valg1", "flervalg1.valg2"))
        assertNotEquals(flervalg, Any())
        assertNotEquals(Flervalg("flervalg1.valg2"), flervalg)
        assertNotEquals(flervalg, Flervalg("flervalg1.valg2"))
    }

    @Test
    fun `er regel for flervalg`() {
        val flervalg = søknad.flervalg(1)
        flervalg.besvar(Flervalg("flervalg1.valg3"))
        val erRegel: Subsumsjon = flervalg inneholder Flervalg("flervalg1.valg3")
        assertTrue { erRegel.resultat()!! }
        flervalg.besvar(Flervalg("flervalg1.valg3", "flervalg1.valg2"))
        assertTrue { erRegel.resultat()!! }
        flervalg.besvar(Flervalg("flervalg1.valg1", "flervalg1.valg2"))
        assertFalse { erRegel.resultat()!! }
    }
}
