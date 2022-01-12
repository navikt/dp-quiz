package no.nav.dagpenger.model.unit.faktum

import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.envalg
import no.nav.dagpenger.model.faktum.Envalg
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.helpers.testSøknadprosess
import no.nav.dagpenger.model.helpers.testversjon
import no.nav.dagpenger.model.seksjon.Søknadprosess
import no.nav.dagpenger.model.subsumsjon.TomSubsumsjon
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ValgFaktumTest {

    val prototypeSøknad = Søknad(
        testversjon,
        envalg faktum "envalg" med "valg1" med "valg2" id 1
    )

    lateinit var søknad: Søknadprosess

    @BeforeEach
    fun setup() {
        søknad = prototypeSøknad.testSøknadprosess(TomSubsumsjon)
    }

    @Test
    fun `Skal kunne være lov å svare med et gyldig valg`() {
        val envalg = søknad.envalg(1)
        assertDoesNotThrow { envalg.besvar(Envalg("valg1")) }
        assertTrue(envalg.erBesvart())
        assertEquals(Envalg("valg1"), envalg.svar())
        assertDoesNotThrow { envalg.besvar(Envalg("valg2")) }
        assertEquals(Envalg("valg2"), envalg.svar())
    }

    @Test
    fun `Skal ikke kunne opprette et tomt Valg`() {
        assertThrows<IllegalArgumentException> { Envalg() }
        assertThrows<IllegalArgumentException> {
            Søknad(
                testversjon,
                envalg faktum "envalg" id 1
            ).testSøknadprosess(TomSubsumsjon)
        }
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
}
