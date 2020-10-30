package no.nav.dagpenger.model.unit.faktum

import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.heltall
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.ja
import no.nav.dagpenger.model.faktagrupper.Faktagrupper
import no.nav.dagpenger.model.faktagrupper.Seksjon
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.faktum.Søknad
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class GenerertFaktumTest {

    @Test
    fun `Enkel template`() {

        val fakta = Søknad(
            ja nei "template" id 1,
            heltall faktum "generator" id 2 genererer 1
        )

        val seksjon = Seksjon("seksjon", Rolle.søker, fakta ja 1, fakta generator 2)
        val faktagrupper = Faktagrupper(fakta, seksjon)
        val originalSize = seksjon.size
        faktagrupper.generator(2).besvar(5)

        assertEquals(5, seksjon.size - originalSize)
        assertEquals("1.1", seksjon[1].id)
        assertEquals("1.5", seksjon[5].id)
    }

    @Test
    fun `Flere templates`() {

        val fakta = Søknad(
            ja nei "template" id 1,
            ja nei "template" id 2,
            ja nei "template" id 3,
            heltall faktum "generer" id 4 genererer 1 og 2 og 3,
            ja nei "boolean" id 5

        )

        val seksjon1 = Seksjon("seksjon", Rolle.søker, fakta ja 1, fakta generator 4)
        val seksjon2 = Seksjon("seksjon", Rolle.søker, fakta ja 2, fakta ja 3, fakta ja 5)
        val faktagrupper = Faktagrupper(fakta, seksjon1, seksjon2)
        val originalSize1 = seksjon1.size
        val originalSize2 = seksjon2.size
        faktagrupper.generator(4).besvar(3)

        assertEquals(3, seksjon1.size - originalSize1)
        assertEquals(6, seksjon2.size - originalSize2)
        assertEquals("1.1", seksjon1[1].id)
        assertEquals("2.1", seksjon2[1].id)
        assertEquals("3.3", seksjon2[7].id)
    }

    @Test
    fun `Generere seksjoner`() {
        val fakta = Søknad(
            ja nei "template" id 1,
            heltall faktum "generator" id 2 genererer 1
        )
        val generatorSeksjon = Seksjon("seksjon", Rolle.søker, fakta generator 2)
        val templateSeksjon = Seksjon("seksjon", Rolle.søker, fakta ja 1)
        val faktagrupper = Faktagrupper(fakta, generatorSeksjon, templateSeksjon)
        faktagrupper.generator(2).besvar(3)
        assertEquals(5, faktagrupper.size)
        assertEquals(1, generatorSeksjon.size)
        assertEquals(1, templateSeksjon.size)
        assertEquals(1, faktagrupper[4].size)
        assertEquals("1.3", faktagrupper[4][0].id)
    }

    @Test
    fun `Seksjon med kun og flere templates`() {
        val fakta = Søknad(
            ja nei "template" id 1,
            ja nei "template" id 2,
            ja nei "template" id 3,
            heltall faktum "generator" id 4 genererer 1 og 2 og 3
        )

        val generatorSeksjon = Seksjon("seksjon", Rolle.søker, fakta generator 4)
        val templateSeksjon1 = Seksjon("seksjon", Rolle.søker, fakta ja 1, fakta ja 2)
        val templateSeksjon2 = Seksjon("seksjon", Rolle.søker, fakta ja 3)
        val faktagrupper = Faktagrupper(fakta, generatorSeksjon, templateSeksjon1, templateSeksjon2)
        faktagrupper.generator(4).besvar(3)

        assertEquals(9, faktagrupper.size)
        assertEquals(1, generatorSeksjon.size)
        assertEquals(2, templateSeksjon1.size)
        assertEquals(1, templateSeksjon2.size)
        assertEquals(2, faktagrupper[4].size)
        assertEquals("2.3", faktagrupper[4][1].id)
        assertEquals("3.3", faktagrupper[8][0].id)
    }

    private operator fun Seksjon.get(indeks: Int) = this.sortedBy { it.id }[indeks]
}
