package no.nav.dagpenger.model.unit.fakta

import no.nav.dagpenger.model.fakta.Dokument
import no.nav.dagpenger.model.fakta.FaktumFactory
import no.nav.dagpenger.model.fakta.FaktumFactory.Companion.boolean
import no.nav.dagpenger.model.fakta.FaktumFactory.Companion.dato
import no.nav.dagpenger.model.fakta.FaktumFactory.Companion.desimal
import no.nav.dagpenger.model.fakta.FaktumFactory.Companion.dokument
import no.nav.dagpenger.model.fakta.FaktumFactory.Companion.heltall
import no.nav.dagpenger.model.fakta.FaktumFactory.Companion.inntekt
import no.nav.dagpenger.model.fakta.Inntekt.Companion.daglig
import no.nav.dagpenger.model.fakta.Inntekt.Companion.månedlig
import no.nav.dagpenger.model.fakta.Inntekt.Companion.årlig
import no.nav.dagpenger.model.fakta.Rolle
import no.nav.dagpenger.model.helpers.januar
import no.nav.dagpenger.model.søknad.Seksjon
import no.nav.dagpenger.model.søknad.Søknad
import no.nav.dagpenger.model.unit.fakta.FaktumFactoryTest.Companion.Valg.A
import no.nav.dagpenger.model.unit.fakta.FaktumFactoryTest.Companion.Valg.C
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.lang.IllegalStateException
import kotlin.math.roundToInt
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class FaktumFactoryTest {

    companion object {
        internal enum class Valg { A, B, C }
    }

    private object valg { infix fun faktum(navn: String) = FaktumFactory(Valg::class.java, navn) }

    @Test
    fun boolean() {
        (boolean faktum "boolean" id 3).also { factory ->
            assertNotNull(factory)
            assertNotNull(factory.faktum)
            assertNotNull(factory.template)
        }
    }

    @Test fun `boolean factory faktum`() {
        val faktum = (boolean faktum "boolean" id 3).faktum.also {
            Søknad(Seksjon("seksjon", Rolle.søker, it))
        }
        assertFalse(faktum.erBesvart())
        assertThrows<IllegalStateException> { faktum.svar() }
        faktum.besvar(true)
        assertTrue(faktum.erBesvart())
        assertTrue(faktum.svar())
        faktum.besvar(false)
        assertTrue(faktum.erBesvart())
        assertFalse(faktum.svar())
    }

    @Test fun `desimal factory faktum`() {
        val faktum = (desimal faktum "desimal" id 3).faktum.also {
            Søknad(Seksjon("seksjon", Rolle.søker, it))
        }
        assertFalse(faktum.erBesvart())
        assertThrows<IllegalStateException> { faktum.svar() }
        faktum.besvar(6.0)
        assertTrue(faktum.erBesvart())
        assertEquals(6.0, faktum.svar())
        faktum.besvar(3.toDouble())
        assertTrue(faktum.erBesvart())
        assertEquals(3.0, faktum.svar())
    }

    @Test fun `heltall factory faktum`() {
        val faktum = (heltall faktum "heltall" id 3).faktum.also {
            Søknad(Seksjon("seksjon", Rolle.søker, it))
        }
        assertFalse(faktum.erBesvart())
        assertThrows<IllegalStateException> { faktum.svar() }
        faktum.besvar(6)
        assertTrue(faktum.erBesvart())
        assertEquals(6, faktum.svar())
        faktum.besvar(3.2.roundToInt())
        assertTrue(faktum.erBesvart())
        assertEquals(3, faktum.svar())
    }

    @Test fun `Inntekt factory faktum`() {
        val faktum = (inntekt faktum "inntekt" id 3).faktum.also {
            Søknad(Seksjon("seksjon", Rolle.søker, it))
        }
        assertFalse(faktum.erBesvart())
        assertThrows<IllegalStateException> { faktum.svar() }
        faktum.besvar(260.årlig)
        assertTrue(faktum.erBesvart())
        assertEquals(1.daglig, faktum.svar())
        faktum.besvar(2.månedlig)
        assertTrue(faktum.erBesvart())
        assertEquals(24.årlig, faktum.svar())
    }

    @Test fun `Dato factory faktum`() {
        val faktum = (dato faktum "dato" id 3).faktum.also {
            Søknad(Seksjon("seksjon", Rolle.søker, it))
        }
        assertFalse(faktum.erBesvart())
        assertThrows<IllegalStateException> { faktum.svar() }
        faktum.besvar(6.januar)
        assertTrue(faktum.erBesvart())
        assertEquals(6.januar, faktum.svar())
        faktum.besvar(2.januar(2020))
        assertTrue(faktum.erBesvart())
        assertEquals(2.januar(2020), faktum.svar())
    }

    @Test fun `Dokument factory faktum`() {
        val faktum = (dokument faktum "dokument" id 3).faktum.also {
            Søknad(Seksjon("seksjon", Rolle.søker, it))
        }
        assertFalse(faktum.erBesvart())
        assertThrows<IllegalStateException> { faktum.svar() }
        Dokument(1.januar).also {
            faktum.besvar(it)
            assertTrue(faktum.erBesvart())
            assertEquals(it, faktum.svar())
        }
    }

    @Test fun `valg (enum) factory faktum`() {
        val faktum = (valg faktum "valg" id 3).faktum.also {
            Søknad(Seksjon("seksjon", Rolle.søker, it))
        }
        assertFalse(faktum.erBesvart())
        assertThrows<IllegalStateException> { faktum.svar() }

        faktum.besvar(A)
        assertTrue(faktum.erBesvart())
        assertEquals(A, faktum.svar())
        faktum.besvar(C)
        assertTrue(faktum.erBesvart())
        assertEquals(C, faktum.svar())
    }
}
