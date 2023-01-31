package no.nav.dagpenger.model.unit.faktum

import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.heltall
import no.nav.dagpenger.model.faktum.Fakta
import no.nav.dagpenger.model.helpers.testSøknadprosess
import no.nav.dagpenger.model.helpers.testversjon
import no.nav.dagpenger.model.regel.er
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

internal class HeltallFaktumTest {

    @Test
    fun `Støtte fakta med type heltall`() {
        val intFaktum = Fakta(testversjon, heltall faktum "int" id 1).testSøknadprosess().let { it heltall 1 }

        assertThrows<IllegalStateException> { intFaktum.svar() }
        intFaktum.besvar(5)
        assertEquals(5, intFaktum.svar())
    }

    @Test
    fun `Subsumsjon med fakta av typen heltall`() {
        val intFaktum = Fakta(testversjon, heltall faktum "int" id 1).testSøknadprosess().let { it heltall 1 }
        val subsumsjon = intFaktum er 0

        assertEquals(null, subsumsjon.resultat())
        intFaktum.besvar(0)
        assertEquals(true, subsumsjon.resultat())
        intFaktum.besvar(5)
        assertEquals(false, subsumsjon.resultat())
    }
}
