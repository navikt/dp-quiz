package no.nav.dagpenger.model.unit.faktum

import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.boolsk
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.heltall
import no.nav.dagpenger.model.faktum.Fakta
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.helpers.testversjon
import no.nav.dagpenger.model.seksjon.Prosess
import no.nav.dagpenger.model.seksjon.Seksjon
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class GenerertFaktumTest {

    @Test
    fun `Enkel template`() {

        val fakta = Fakta(
            testversjon,
            boolsk faktum "template" id 1,
            heltall faktum "generator" id 2 genererer 1
        )

        val seksjon = Seksjon("seksjon", Rolle.søker, fakta boolsk 1, fakta generator 2)
        val prosess = Prosess(fakta, seksjon)
        val originalSize = seksjon.size
        prosess.generator(2).besvar(5)

        assertEquals(5, seksjon.size - originalSize)
        assertEquals("1.1", seksjon[1].id)
        assertEquals("1.5", seksjon[5].id)
    }

    @Test
    fun `Flere templates`() {

        val fakta = Fakta(
            testversjon,
            boolsk faktum "template" id 1,
            boolsk faktum "template" id 2,
            boolsk faktum "template" id 3,
            heltall faktum "generer" id 4 genererer 1 og 2 og 3,
            boolsk faktum "boolean" id 5

        )

        val seksjon1 = Seksjon("seksjon", Rolle.søker, fakta boolsk 1, fakta generator 4)
        val seksjon2 = Seksjon("seksjon", Rolle.søker, fakta boolsk 2, fakta boolsk 3, fakta boolsk 5)
        val prosess = Prosess(fakta, seksjon1, seksjon2)
        val originalSize1 = seksjon1.size
        val originalSize2 = seksjon2.size
        prosess.generator(4).besvar(3)

        assertEquals(3, seksjon1.size - originalSize1)
        assertEquals(6, seksjon2.size - originalSize2)
        assertEquals("1.1", seksjon1[1].id)
        assertEquals("2.1", seksjon2[1].id)
        assertEquals("3.3", seksjon2[7].id)
    }

    @Test
    fun `generator faktum med avhengighet skal resettes`() {

        val fakta = Fakta(
            testversjon,
            boolsk faktum "template" id 1,
            boolsk faktum "template" id 2,
            boolsk faktum "template" id 3,
            heltall faktum "generer" id 4 genererer 1 og 2 og 3 avhengerAv 5,
            boolsk faktum "boolean" id 5
        )

        val seksjon1 = Seksjon("seksjon", Rolle.søker, fakta boolsk 1, fakta generator 4)
        val seksjon2 = Seksjon("seksjon", Rolle.søker, fakta boolsk 2, fakta boolsk 3, fakta boolsk 5)
        val prosess = Prosess(fakta, seksjon1, seksjon2)

        prosess.boolsk(5).besvar(true)

        prosess.heltall(4).besvar(1)
        prosess.boolsk("1.1").besvar(true)
        prosess.boolsk("2.1").besvar(true)
        prosess.boolsk("3.1").besvar(true)

        assertTrue("1.1 skal være besvart") { prosess.boolsk("1.1").erBesvart() }
        assertTrue("2.1 skal være besvart") { prosess.boolsk("2.1").erBesvart() }
        assertTrue("3.1 skal være besvart") { prosess.boolsk("3.1").erBesvart() }
        assertTrue("4 skal være besvart") { prosess.boolsk(4).erBesvart() }

        prosess.boolsk(5).besvar(false)

        assertFalse("4 skal ikke være besvart") { prosess.boolsk(4).erBesvart() }
        assertNull(prosess.fakta.find { it.id == "1.1" }, "1.1 skal ikke være besvart")
        assertNull(prosess.fakta.find { it.id == "2.1" }, "2.1 skal ikke være besvart")
        assertNull(prosess.fakta.find { it.id == "3.1" }, "3.1 skal ikke være besvart")
    }

    @Test
    fun `Generere seksjoner`() {
        val fakta = Fakta(
            testversjon,
            boolsk faktum "template" id 1,
            heltall faktum "generator" id 2 genererer 1
        )
        val generatorSeksjon = Seksjon("seksjon", Rolle.søker, fakta generator 2)
        val templateSeksjon = Seksjon("seksjon", Rolle.søker, fakta boolsk 1)
        val prosess = Prosess(fakta, generatorSeksjon, templateSeksjon)
        prosess.generator(2).besvar(3)
        assertEquals(5, prosess.size)
        assertEquals(1, generatorSeksjon.size)
        assertEquals(1, templateSeksjon.size)
        assertEquals(1, prosess[4].size)
        assertEquals("1.3", prosess[4][0].id)
    }

    @Test
    fun `Seksjon med kun og flere templates`() {
        val fakta = Fakta(
            testversjon,
            boolsk faktum "template" id 1,
            boolsk faktum "template" id 2,
            boolsk faktum "template" id 3,
            heltall faktum "generator" id 4 genererer 1 og 2 og 3
        )

        val generatorSeksjon = Seksjon("seksjon", Rolle.søker, fakta generator 4)
        val templateSeksjon1 = Seksjon("seksjon", Rolle.søker, fakta boolsk 1, fakta boolsk 2)
        val templateSeksjon2 = Seksjon("seksjon", Rolle.søker, fakta boolsk 3)
        val prosess = Prosess(fakta, generatorSeksjon, templateSeksjon1, templateSeksjon2)
        prosess.generator(4).besvar(3)

        assertEquals(9, prosess.size)
        assertEquals(1, generatorSeksjon.size)
        assertEquals(2, templateSeksjon1.size)
        assertEquals(1, templateSeksjon2.size)
        assertEquals(2, prosess[4].size)
        assertEquals("2.3", prosess[4][1].id)
        assertEquals("3.3", prosess[8][0].id)
    }

    private operator fun Seksjon.get(indeks: Int) = this.sortedBy { it.id }[indeks]
}
