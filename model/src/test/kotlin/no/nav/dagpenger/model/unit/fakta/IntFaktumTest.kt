package no.nav.dagpenger.model.unit.fakta

import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.heltall
import no.nav.dagpenger.model.fakta.Fakta
import no.nav.dagpenger.model.helpers.testSøknad
import no.nav.dagpenger.model.regel.er
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

internal class IntFaktumTest {

    @Test
    fun `Støtte fakta med type int`() {
        val intFaktum = Fakta(heltall faktum "int" id 1).testSøknad().let { it heltall 1 }

        assertThrows<IllegalStateException> { intFaktum.svar() }
        intFaktum.besvar(5)
        assertEquals(5, intFaktum.svar())
    }

    @Test
    fun `Subsumsjon med fakta av typen int`() {
        val intFaktum = Fakta(heltall faktum "int" id 1).testSøknad().let { it heltall 1 }
        val subsumsjon = intFaktum er 0

        assertEquals(null, subsumsjon.resultat())
        intFaktum.besvar(0)
        assertEquals(true, subsumsjon.resultat())
        intFaktum.besvar(5)
        assertEquals(false, subsumsjon.resultat())
    }
}
