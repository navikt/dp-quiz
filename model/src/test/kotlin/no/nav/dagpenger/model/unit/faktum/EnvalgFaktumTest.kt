package no.nav.dagpenger.model.unit.faktum

import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.dato
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.envalg
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.heltall
import no.nav.dagpenger.model.faktum.Envalg
import no.nav.dagpenger.model.faktum.Fakta
import no.nav.dagpenger.model.helpers.testSøknadprosess
import no.nav.dagpenger.model.helpers.testversjon
import no.nav.dagpenger.model.regel.inneholder
import no.nav.dagpenger.model.seksjon.Faktagrupper
import no.nav.dagpenger.model.subsumsjon.TomSubsumsjon
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class EnvalgFaktumTest {

    val prototypeFakta = Fakta(
        testversjon,
        envalg faktum "envalg" med "valg1" med "valg2" id 1,
        envalg faktum "envalg2" med "valg1" med "valg2" med "valg3" id 2,
        dato faktum "dato" id 3,
        heltall faktum "generator" id 4 genererer 2 og 3
    )

    lateinit var søknad: Faktagrupper

    @BeforeEach
    fun setup() {
        søknad = prototypeFakta.testSøknadprosess(TomSubsumsjon)
    }

    @Test
    fun `Skal kunne være lov å svare med et gyldig valg`() {
        val envalg = søknad.envalg(1)
        assertDoesNotThrow { envalg.besvar(Envalg("envalg.valg1")) }
        assertTrue(envalg.erBesvart())
        assertEquals(Envalg("envalg.valg1"), envalg.svar())
        assertDoesNotThrow { envalg.besvar(Envalg("envalg.valg2")) }
        assertEquals(Envalg("envalg.valg2"), envalg.svar())
    }

    @Test
    fun `Skal ikke kunne opprette et tomt Valg`() {
        assertThrows<IllegalArgumentException> { Envalg() }
        assertThrows<IllegalArgumentException> {
            Fakta(
                testversjon,
                envalg faktum "envalg" id 1
            ).testSøknadprosess(TomSubsumsjon)
        }
    }

    @Test
    fun `envalg kan være template faktum`() {
        søknad.generator(4).besvar(1)
        val envalg = søknad.envalg("2.1")
        envalg.besvar(Envalg("envalg2.valg3"))
        assertTrue(envalg.erBesvart())
        assertEquals(Envalg("envalg2.valg3"), envalg.svar())
    }

    @Test
    fun `Skal kaste feil hvis en svarer med ugyldige valg`() {
        val envalg = søknad.envalg(1)
        assertThrows<IllegalArgumentException> { envalg.besvar(Envalg("ugyldig-valg")) }
    }

    @Test
    fun `Skal kaste feil hvis flere gyldige alternativer velges`() {
        val envalg = søknad.envalg(1)
        assertThrows<IllegalArgumentException> { envalg.besvar(Envalg("valg1", "valg2")) }
    }

    @Test
    fun `likhet test `() {
        val envalg = Envalg("envalg1.valg1", "envalg2.valg2")
        assertEquals(envalg, envalg)
        assertEquals(envalg, Envalg("envalg1.valg1", "envalg2.valg2"))
        assertNotEquals(envalg, Any())
        assertNotEquals(Envalg("envalg1.valg2"), envalg)
        assertNotEquals(envalg, Envalg("envalg1.valg2"))
    }

    @Test
    fun `envalg subsumsjon `() {
        val envalg = søknad.envalg(1)
        envalg.besvar(Envalg("envalg.valg1"))
        val erValgSubsumsjon = envalg inneholder Envalg("envalg.valg1")
        assertTrue { erValgSubsumsjon.resultat()!! }
        envalg.besvar(Envalg("envalg.valg2"))
        assertFalse { erValgSubsumsjon.resultat()!! }
    }
}
