package no.nav.dagpenger.model.unit.faktum

import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.flervalg
import no.nav.dagpenger.model.faktum.Flervalg
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

class FlervalgFaktumTest {

    val prototypeSøknad = Søknad(
        testversjon,
        flervalg faktum "flervalg" med "valg1" med "valg2" med "valg3" id 1
    )

    lateinit var søknad: Søknadprosess

    @BeforeEach
    fun setup() {
        søknad = prototypeSøknad.testSøknadprosess(TomSubsumsjon)
    }

    @Test
    fun `Skal kunne være lov å svare med gyldige valg`() {
        val flervalg = søknad.flervalg(1)
        assertDoesNotThrow { flervalg.besvar(Flervalg("valg1", "valg2")) }
        assertTrue(flervalg.erBesvart())
        assertEquals(Flervalg("valg1", "valg2"), flervalg.svar())
    }

    @Test
    fun `Skal ikke kunne opprette et tomt Valg`() {
        assertThrows<IllegalArgumentException> { Flervalg() }
        assertThrows<IllegalArgumentException> {
            Søknad(
                testversjon,
                flervalg faktum "flervalg" id 1
            ).testSøknadprosess(TomSubsumsjon)
        }
    }

    @Test
    fun `Skal kaste feil hvis en svarer med ugyldige valg`() {
        val flervalg = søknad.flervalg(1)
        assertThrows<IllegalArgumentException> { flervalg.besvar(Flervalg("ugyldig-valg")) }
    }
}