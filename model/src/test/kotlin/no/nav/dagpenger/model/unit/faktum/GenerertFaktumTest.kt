package no.nav.dagpenger.model.unit.faktum

import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.boolsk
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.heltall
import no.nav.dagpenger.model.faktum.Fakta
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.helpers.testversjon
import no.nav.dagpenger.model.seksjon.Seksjon
import no.nav.dagpenger.model.seksjon.Utredningsprosess
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
        val utredningsprosess = Utredningsprosess(fakta, seksjon)
        val originalSize = seksjon.size
        utredningsprosess.generator(2).besvar(5)

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
        val utredningsprosess = Utredningsprosess(fakta, seksjon1, seksjon2)
        val originalSize1 = seksjon1.size
        val originalSize2 = seksjon2.size
        utredningsprosess.generator(4).besvar(3)

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
        val utredningsprosess = Utredningsprosess(fakta, seksjon1, seksjon2)

        utredningsprosess.boolsk(5).besvar(true)

        utredningsprosess.heltall(4).besvar(1)
        utredningsprosess.boolsk("1.1").besvar(true)
        utredningsprosess.boolsk("2.1").besvar(true)
        utredningsprosess.boolsk("3.1").besvar(true)

        assertTrue("1.1 skal være besvart") { utredningsprosess.boolsk("1.1").erBesvart() }
        assertTrue("2.1 skal være besvart") { utredningsprosess.boolsk("2.1").erBesvart() }
        assertTrue("3.1 skal være besvart") { utredningsprosess.boolsk("3.1").erBesvart() }
        assertTrue("4 skal være besvart") { utredningsprosess.boolsk(4).erBesvart() }

        utredningsprosess.boolsk(5).besvar(false)

        assertFalse("4 skal ikke være besvart") { utredningsprosess.boolsk(4).erBesvart() }
        assertNull(utredningsprosess.fakta.find { it.id == "1.1" }, "1.1 skal ikke være besvart")
        assertNull(utredningsprosess.fakta.find { it.id == "2.1" }, "2.1 skal ikke være besvart")
        assertNull(utredningsprosess.fakta.find { it.id == "3.1" }, "3.1 skal ikke være besvart")
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
        val utredningsprosess = Utredningsprosess(fakta, generatorSeksjon, templateSeksjon)
        utredningsprosess.generator(2).besvar(3)
        assertEquals(5, utredningsprosess.size)
        assertEquals(1, generatorSeksjon.size)
        assertEquals(1, templateSeksjon.size)
        assertEquals(1, utredningsprosess[4].size)
        assertEquals("1.3", utredningsprosess[4][0].id)
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
        val utredningsprosess = Utredningsprosess(fakta, generatorSeksjon, templateSeksjon1, templateSeksjon2)
        utredningsprosess.generator(4).besvar(3)

        assertEquals(9, utredningsprosess.size)
        assertEquals(1, generatorSeksjon.size)
        assertEquals(2, templateSeksjon1.size)
        assertEquals(1, templateSeksjon2.size)
        assertEquals(2, utredningsprosess[4].size)
        assertEquals("2.3", utredningsprosess[4][1].id)
        assertEquals("3.3", utredningsprosess[8][0].id)
    }

    private operator fun Seksjon.get(indeks: Int) = this.sortedBy { it.id }[indeks]
}
