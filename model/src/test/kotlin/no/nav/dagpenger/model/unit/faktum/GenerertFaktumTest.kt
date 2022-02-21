package no.nav.dagpenger.model.unit.faktum

import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.boolsk
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.heltall
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.helpers.testversjon
import no.nav.dagpenger.model.seksjon.Seksjon
import no.nav.dagpenger.model.seksjon.Søknadprosess
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class GenerertFaktumTest {

    @Test
    fun `Enkel template`() {

        val søknad = Søknad(
            testversjon,
            boolsk faktum "template" id 1,
            heltall faktum "generator" id 2 genererer 1
        )

        val seksjon = Seksjon("seksjon", Rolle.søker, søknad boolsk 1, søknad generator 2)
        val søknadprosess = Søknadprosess(søknad, seksjon)
        val originalSize = seksjon.size
        søknadprosess.generator(2).besvar(5)

        assertEquals(5, seksjon.size - originalSize)
        assertEquals("1.1", seksjon[1].id)
        assertEquals("1.5", seksjon[5].id)
    }

    @Test
    fun `Flere templates`() {

        val søknad = Søknad(
            testversjon,
            boolsk faktum "template" id 1,
            boolsk faktum "template" id 2,
            boolsk faktum "template" id 3,
            heltall faktum "generer" id 4 genererer 1 og 2 og 3,
            boolsk faktum "boolean" id 5

        )

        val seksjon1 = Seksjon("seksjon", Rolle.søker, søknad boolsk 1, søknad generator 4)
        val seksjon2 = Seksjon("seksjon", Rolle.søker, søknad boolsk 2, søknad boolsk 3, søknad boolsk 5)
        val søknadprosess = Søknadprosess(søknad, seksjon1, seksjon2)
        val originalSize1 = seksjon1.size
        val originalSize2 = seksjon2.size
        søknadprosess.generator(4).besvar(3)

        assertEquals(3, seksjon1.size - originalSize1)
        assertEquals(6, seksjon2.size - originalSize2)
        assertEquals("1.1", seksjon1[1].id)
        assertEquals("2.1", seksjon2[1].id)
        assertEquals("3.3", seksjon2[7].id)
    }

    @Test
    fun `generator faktum med avhengighet skal resettes`() {

        val søknad = Søknad(
            testversjon,
            boolsk faktum "template" id 1,
            boolsk faktum "template" id 2,
            boolsk faktum "template" id 3,
            heltall faktum "generer" id 4 genererer 1 og 2 og 3 avhengerAv 5,
            boolsk faktum "boolean" id 5
        )

        val seksjon1 = Seksjon("seksjon", Rolle.søker, søknad boolsk 1, søknad generator 4)
        val seksjon2 = Seksjon("seksjon", Rolle.søker, søknad boolsk 2, søknad boolsk 3, søknad boolsk 5)
        val søknadprosess = Søknadprosess(søknad, seksjon1, seksjon2)

        søknadprosess.boolsk(5).besvar(true)

        søknadprosess.heltall(4).besvar(1)
        søknadprosess.boolsk("1.1").besvar(true)
        søknadprosess.boolsk("2.1").besvar(true)
        søknadprosess.boolsk("3.1").besvar(true)

        assertTrue("1.1 skal være besvart") { søknadprosess.boolsk("1.1").erBesvart() }
        assertTrue("2.1 skal være besvart") { søknadprosess.boolsk("2.1").erBesvart() }
        assertTrue("3.1 skal være besvart") { søknadprosess.boolsk("3.1").erBesvart() }
        assertTrue("4 skal være besvart") { søknadprosess.boolsk(4).erBesvart() }

        søknadprosess.boolsk(5).besvar(false)

        assertFalse("4 skal ikke være besvart") { søknadprosess.boolsk(4).erBesvart() }
        assertNull(søknadprosess.søknad.find { it.id == "1.1" }, "1.1 skal ikke være besvart")
        assertNull(søknadprosess.søknad.find { it.id == "2.1" }, "2.1 skal ikke være besvart")
        assertNull(søknadprosess.søknad.find { it.id == "3.1" }, "3.1 skal ikke være besvart")
    }

    @Test
    fun `Generere seksjoner`() {
        val søknad = Søknad(
            testversjon,
            boolsk faktum "template" id 1,
            heltall faktum "generator" id 2 genererer 1
        )
        val generatorSeksjon = Seksjon("seksjon", Rolle.søker, søknad generator 2)
        val templateSeksjon = Seksjon("seksjon", Rolle.søker, søknad boolsk 1)
        val søknadprosess = Søknadprosess(søknad, generatorSeksjon, templateSeksjon)
        søknadprosess.generator(2).besvar(3)
        assertEquals(5, søknadprosess.size)
        assertEquals(1, generatorSeksjon.size)
        assertEquals(1, templateSeksjon.size)
        assertEquals(1, søknadprosess[4].size)
        assertEquals("1.3", søknadprosess[4][0].id)
    }

    @Test
    fun `Seksjon med kun og flere templates`() {
        val søknad = Søknad(
            testversjon,
            boolsk faktum "template" id 1,
            boolsk faktum "template" id 2,
            boolsk faktum "template" id 3,
            heltall faktum "generator" id 4 genererer 1 og 2 og 3
        )

        val generatorSeksjon = Seksjon("seksjon", Rolle.søker, søknad generator 4)
        val templateSeksjon1 = Seksjon("seksjon", Rolle.søker, søknad boolsk 1, søknad boolsk 2)
        val templateSeksjon2 = Seksjon("seksjon", Rolle.søker, søknad boolsk 3)
        val søknadprosess = Søknadprosess(søknad, generatorSeksjon, templateSeksjon1, templateSeksjon2)
        søknadprosess.generator(4).besvar(3)

        assertEquals(9, søknadprosess.size)
        assertEquals(1, generatorSeksjon.size)
        assertEquals(2, templateSeksjon1.size)
        assertEquals(1, templateSeksjon2.size)
        assertEquals(2, søknadprosess[4].size)
        assertEquals("2.3", søknadprosess[4][1].id)
        assertEquals("3.3", søknadprosess[8][0].id)
    }

    private operator fun Seksjon.get(indeks: Int) = this.sortedBy { it.id }[indeks]
}
