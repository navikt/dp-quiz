package no.nav.dagpenger.model.unit.faktum

import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.valg
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.faktum.Valg
import no.nav.dagpenger.model.helpers.testSøknadprosess
import no.nav.dagpenger.model.helpers.testversjon
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class ValgFaktumTest {
    val søknad = Søknad(
        testversjon,
        valg faktum "valg" id 1 med "valg1" med "valg2",
    ).testSøknadprosess()

    @Test
    fun `Skal kun være lov å svare med gyldige valg`() {
        val valgFaktum = søknad valg 1

        assertThrows<IllegalArgumentException> { valgFaktum.besvar(Valg("bla")) }
        assertDoesNotThrow { valgFaktum.besvar(Valg("valg1")) }
        assertDoesNotThrow { valgFaktum.besvar(Valg("valg2")) }
    }

    @Test
    fun `Skal kunne besvare valgFaktum med flere valg`() {
        val valgFaktum = søknad valg 1

        assertFalse { valgFaktum.erBesvart() }
        valgFaktum.besvar(Valg("valg2", "valg1"))
        assertEquals(Valg("valg1", "valg2"), valgFaktum.svar())
        assertTrue { valgFaktum.erBesvart() }
    }

    @Test
    fun `Besvare et valgFaktum med bare ett svar`() {
        val valgFaktum = søknad valg 1
        val valg = Valg("valg1")

        assertFalse { valgFaktum.erBesvart() }
        assertDoesNotThrow { valgFaktum.besvar(valg) }
        assertEquals(valg, valgFaktum.svar())
        assertTrue { valgFaktum.erBesvart() }
    }

    @Test
    fun `Skal ikke gå å lage valgfaktum uten valg`() {
        assertThrows<IllegalArgumentException> {
            Søknad(
                testversjon,
                valg faktum "valg" id 1
            ).testSøknadprosess()
        }
    }
}
