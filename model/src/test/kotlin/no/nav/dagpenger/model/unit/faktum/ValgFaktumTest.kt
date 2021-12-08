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

    @Test
    fun `FlervalgFaktum - Støtter å ta i mot predefinerte svarternativer`() {
        val søknad = Søknad(
            testversjon,
            valg faktum "valg" id 1 med "valg1" med "valg2",
        ).testSøknadprosess()
        val valgFaktum = søknad valg 1

        assertFalse { valgFaktum.erBesvart() }
        assertThrows<IllegalArgumentException> { valgFaktum.besvar(Valg("bla")) }
        assertDoesNotThrow { valgFaktum.besvar(Valg("valg1")) }
        assertDoesNotThrow { valgFaktum.besvar(Valg("valg2")) }
        assertTrue { valgFaktum.erBesvart() }
        assertEquals(Valg("valg2"), valgFaktum.svar())
        valgFaktum.besvar(Valg("valg2", "valg1"))
        assertEquals(Valg("valg1", "valg2"), valgFaktum.svar())
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
