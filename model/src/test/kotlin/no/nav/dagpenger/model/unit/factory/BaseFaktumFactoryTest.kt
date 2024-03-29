package no.nav.dagpenger.model.unit.factory

import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.boolsk
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.dato
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.dokument
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.heltall
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.inntekt
import no.nav.dagpenger.model.faktum.Dokument
import no.nav.dagpenger.model.faktum.Fakta
import no.nav.dagpenger.model.faktum.Inntekt.Companion.daglig
import no.nav.dagpenger.model.faktum.Inntekt.Companion.månedlig
import no.nav.dagpenger.model.faktum.Inntekt.Companion.årlig
import no.nav.dagpenger.model.helpers.januar
import no.nav.dagpenger.model.helpers.testSøknadprosess
import no.nav.dagpenger.model.helpers.testversjon
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.math.roundToInt
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class BaseFaktumFactoryTest {

    @Test
    fun boolean() {
        (boolsk faktum "boolean" id 3).also { factory ->
            assertNotNull(factory)
            assertNotNull(factory.faktum())
        }
    }

    @Test
    fun `boolean factory faktum`() {
        val søknadprosess = Fakta(testversjon, boolsk faktum "boolean" id 3).testSøknadprosess()
        val faktum = søknadprosess boolsk 3
        assertFalse(faktum.erBesvart())
        assertThrows<IllegalStateException> { faktum.svar() }
        faktum.besvar(true)
        assertTrue(faktum.erBesvart())
        assertTrue(faktum.svar())
        faktum.besvar(false)
        assertTrue(faktum.erBesvart())
        assertFalse(faktum.svar())
    }

    @Test
    fun `heltall factory faktum`() {
        val søknadprosess = Fakta(testversjon, heltall faktum "heltall" id 3).testSøknadprosess()
        val faktum = søknadprosess heltall 3
        assertFalse(faktum.erBesvart())
        assertThrows<IllegalStateException> { faktum.svar() }
        faktum.besvar(6)
        assertTrue(faktum.erBesvart())
        assertEquals(6, faktum.svar())
        faktum.besvar(3.2.roundToInt())
        assertTrue(faktum.erBesvart())
        assertEquals(3, faktum.svar())
    }

    @Test
    fun `Inntekt factory faktum`() {
        val søknadprosess = Fakta(testversjon, inntekt faktum "inntekt" id 3).testSøknadprosess()
        val faktum = søknadprosess inntekt 3
        assertFalse(faktum.erBesvart())
        assertThrows<IllegalStateException> { faktum.svar() }
        faktum.besvar(260.årlig)
        assertTrue(faktum.erBesvart())
        assertEquals(1.daglig, faktum.svar())
        faktum.besvar(2.månedlig)
        assertTrue(faktum.erBesvart())
        assertEquals(24.årlig, faktum.svar())
    }

    @Test
    fun `Dato factory faktum`() {
        val søknadprosess = Fakta(testversjon, dato faktum "dato" id 3).testSøknadprosess()
        val faktum = søknadprosess dato 3
        assertFalse(faktum.erBesvart())
        assertThrows<IllegalStateException> { faktum.svar() }
        faktum.besvar(6.januar)
        assertTrue(faktum.erBesvart())
        assertEquals(6.januar, faktum.svar())
        faktum.besvar(2.januar(2020))
        assertTrue(faktum.erBesvart())
        assertEquals(2.januar(2020), faktum.svar())
    }

    @Test
    fun `Dokument factory faktum`() {
        val søknadprosess = Fakta(testversjon, dokument faktum "dokument" id 3).testSøknadprosess()
        val faktum = søknadprosess dokument 3
        assertFalse(faktum.erBesvart())
        assertThrows<IllegalStateException> { faktum.svar() }
        Dokument(1.januar, "urn:nid:sse").also {
            faktum.besvar(it)
            assertTrue(faktum.erBesvart())
            assertEquals(it, faktum.svar())
        }
    }

    @Test
    fun `Et faktum kan ikke avhengerAv seg selv`() {
        assertThrows<IllegalArgumentException> {
            boolsk faktum "boolean" id 1 avhengerAv 1
        }
    }
}
