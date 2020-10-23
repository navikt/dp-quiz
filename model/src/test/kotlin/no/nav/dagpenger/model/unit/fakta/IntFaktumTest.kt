package no.nav.dagpenger.model.unit.fakta

import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.heltall
import no.nav.dagpenger.model.fakta.Rolle
import no.nav.dagpenger.model.fakta.faktum
import no.nav.dagpenger.model.regel.er
import no.nav.dagpenger.model.søknad.Seksjon
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.lang.IllegalStateException
import kotlin.test.assertEquals

internal class IntFaktumTest {

    @Test
    fun `Støtte fakta med type int`() {
        val intFaktum = (heltall faktum "int" id 1).faktum()
        val seksjon = Seksjon("seksjon", Rolle.søker, intFaktum)
        assertThrows<IllegalStateException> { intFaktum.svar() }
        intFaktum.besvar(5)
        assertEquals(5, intFaktum.svar())
    }

    @Test
    fun `Subsumsjon med fakta av typen int`() {
        val intFaktum = (heltall faktum "int" id 1).faktum()
        val subsumsjon = intFaktum er 0
        val seksjon = Seksjon("seksjon", Rolle.søker, intFaktum)

        assertEquals(null, subsumsjon.resultat())
        intFaktum.besvar(0)
        assertEquals(true, subsumsjon.resultat())
        intFaktum.besvar(5)
        assertEquals(false, subsumsjon.resultat())
    }
}
