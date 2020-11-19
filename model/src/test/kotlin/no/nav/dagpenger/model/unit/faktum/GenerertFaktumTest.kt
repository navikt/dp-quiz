package no.nav.dagpenger.model.unit.faktum

import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.heltall
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.ja
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.seksjon.Seksjon
import no.nav.dagpenger.model.seksjon.Søknadprosess
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class GenerertFaktumTest {

    @Test
    fun `Enkel template`() {

        val søknad = Søknad(
            49,
            ja nei "template" id 1,
            heltall faktum "generator" id 2 genererer 1
        )

        val seksjon = Seksjon("seksjon", Rolle.søker, søknad ja 1, søknad generator 2)
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
            48,
            ja nei "template" id 1,
            ja nei "template" id 2,
            ja nei "template" id 3,
            heltall faktum "generer" id 4 genererer 1 og 2 og 3,
            ja nei "boolean" id 5

        )

        val seksjon1 = Seksjon("seksjon", Rolle.søker, søknad ja 1, søknad generator 4)
        val seksjon2 = Seksjon("seksjon", Rolle.søker, søknad ja 2, søknad ja 3, søknad ja 5)
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
    fun `Generere seksjoner`() {
        val søknad = Søknad(
            47,
            ja nei "template" id 1,
            heltall faktum "generator" id 2 genererer 1
        )
        val generatorSeksjon = Seksjon("seksjon", Rolle.søker, søknad generator 2)
        val templateSeksjon = Seksjon("seksjon", Rolle.søker, søknad ja 1)
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
            46,
            ja nei "template" id 1,
            ja nei "template" id 2,
            ja nei "template" id 3,
            heltall faktum "generator" id 4 genererer 1 og 2 og 3
        )

        val generatorSeksjon = Seksjon("seksjon", Rolle.søker, søknad generator 4)
        val templateSeksjon1 = Seksjon("seksjon", Rolle.søker, søknad ja 1, søknad ja 2)
        val templateSeksjon2 = Seksjon("seksjon", Rolle.søker, søknad ja 3)
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
