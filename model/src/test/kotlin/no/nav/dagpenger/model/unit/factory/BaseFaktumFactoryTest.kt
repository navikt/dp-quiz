package no.nav.dagpenger.model.unit.factory

import no.nav.dagpenger.model.factory.BaseFaktumFactory
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.dato
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.dokument
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.heltall
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.inntekt
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.ja
import no.nav.dagpenger.model.fakta.Dokument
import no.nav.dagpenger.model.fakta.Fakta
import no.nav.dagpenger.model.fakta.Faktum
import no.nav.dagpenger.model.fakta.Inntekt.Companion.daglig
import no.nav.dagpenger.model.fakta.Inntekt.Companion.månedlig
import no.nav.dagpenger.model.fakta.Inntekt.Companion.årlig
import no.nav.dagpenger.model.helpers.januar
import no.nav.dagpenger.model.helpers.testSøknad
import no.nav.dagpenger.model.unit.factory.BaseFaktumFactoryTest.Companion.Valg.A
import no.nav.dagpenger.model.unit.factory.BaseFaktumFactoryTest.Companion.Valg.C
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.lang.IllegalStateException
import kotlin.math.roundToInt
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class BaseFaktumFactoryTest {

    companion object {
        internal enum class Valg { A, B, C }
    }

    private object valg { infix fun faktum(navn: String) = BaseFaktumFactory(Valg::class.java, navn) }

    // private object choices { fun <T> faktum(navn: String, enum: Enum<T>) = BaseFaktumFactory(enum::class.java, navn) }

    @Test
    fun boolean() {
        (ja nei "boolean" id 3).also { factory ->
            assertNotNull(factory)
            assertNotNull(factory.faktum())
        }
    }

    @Test fun `boolean factory faktum`() {
        val søknad = Fakta(ja nei "boolean" id 3).testSøknad()
        val faktum = søknad ja 3
        assertFalse(faktum.erBesvart())
        assertThrows<IllegalStateException> { faktum.svar() }
        faktum.besvar(true)
        assertTrue(faktum.erBesvart())
        assertTrue(faktum.svar())
        faktum.besvar(false)
        assertTrue(faktum.erBesvart())
        assertFalse(faktum.svar())
    }

    @Test fun `heltall factory faktum`() {
        val søknad = Fakta(heltall faktum "heltall" id 3).testSøknad()
        val faktum = søknad heltall 3
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
        val søknad = Fakta(inntekt faktum "inntekt" id 3).testSøknad()
        val faktum = søknad inntekt 3
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
        val søknad = Fakta(dato faktum "dato" id 3).testSøknad()
        val faktum = søknad dato 3
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
        val søknad = Fakta(dokument faktum "dokument" id 3).testSøknad()
        val faktum = søknad dokument 3
        assertFalse(faktum.erBesvart())
        assertThrows<IllegalStateException> { faktum.svar() }
        Dokument(1.januar).also {
            faktum.besvar(it)
            assertTrue(faktum.erBesvart())
            assertEquals(it, faktum.svar())
        }
    }

    @Test fun `valg (enum) factory faktum`() {
        val søknad = Fakta(valg faktum "valg" id 3).testSøknad()
        val faktum = søknad.id(3) as Faktum<Valg>
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
