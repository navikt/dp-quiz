package no.nav.dagpenger.model.unit.fakta

import no.nav.dagpenger.model.fakta.FaktumNavn
import no.nav.dagpenger.model.fakta.Rolle
import no.nav.dagpenger.model.fakta.faktum
import no.nav.dagpenger.model.fakta.template
import no.nav.dagpenger.model.søknad.Seksjon
import no.nav.dagpenger.model.søknad.Søknad
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class GenerertFaktumTest {

    @Test
    fun `Enkel template`() {
        val template = FaktumNavn(1, "template").template(Boolean::class.java)
        val generator = FaktumNavn(2, "generer").faktum(Int::class.java, template)
        val seksjon = Seksjon(Rolle.søker, template, generator)
        val originalSize = seksjon.size
        generator.besvar(5)

        assertEquals(5, seksjon.size - originalSize)
        assertEquals("1.1", seksjon[1].id)
        assertEquals("1.5", seksjon[5].id)
    }

    @Test
    fun `Flere templates`() {
        val template1 = FaktumNavn(1, "template1").template(Boolean::class.java)
        val template2 = FaktumNavn(2, "template2").template(Boolean::class.java)
        val template3 = FaktumNavn(3, "template3").template(Boolean::class.java)
        val generator = FaktumNavn(4, "generer").faktum(Int::class.java, template1, template2, template3)
        val seksjon1 = Seksjon(Rolle.søker, template1, generator)
        val seksjon2 = Seksjon(Rolle.søker, template2, template3, FaktumNavn(5, "faktum").faktum(Boolean::class.java))
        val søknad = Søknad(seksjon1, seksjon2)
        val originalSize1 = seksjon1.size
        val originalSize2 = seksjon2.size
        generator.besvar(3)

        assertEquals(3, seksjon1.size - originalSize1)
        assertEquals(6, seksjon2.size - originalSize2)
        assertEquals("1.1", seksjon1[1].id)
        assertEquals("2.1", seksjon2[1].id)
        assertEquals("3.3", seksjon2[7].id)
    }

    @Test
    fun `Generere seksjoner`() {
        val template = FaktumNavn(1, "template").template(Boolean::class.java)
        val generator = FaktumNavn(2, "generer").faktum(Int::class.java, template)
        val generatorSeksjon = Seksjon(Rolle.søker, generator)
        val templateSeksjon = Seksjon(Rolle.søker, template)
        val søknad = Søknad(generatorSeksjon, templateSeksjon)
        generator.besvar(3)

        assertEquals(5, søknad.size)
        assertEquals(1, generatorSeksjon.size)
        assertEquals(1, templateSeksjon.size)
        assertEquals(1, søknad[4].size)
        assertEquals("1.3", søknad[4][0].id)
    }

    @Test
    fun `Seksjon med kun og flere templates`() {
        val template1 = FaktumNavn(1, "template1").template(Boolean::class.java)
        val template2 = FaktumNavn(2, "template2").template(Boolean::class.java)
        val template3 = FaktumNavn(3, "template3").template(Boolean::class.java)
        val generator = FaktumNavn(4, "generer").faktum(Int::class.java, template1, template2, template3)
        val generatorSeksjon = Seksjon(Rolle.søker, generator)
        val templateSeksjon1 = Seksjon(Rolle.søker, template1, template2)
        val templateSeksjon2 = Seksjon(Rolle.søker, template3)
        val søknad = Søknad(generatorSeksjon, templateSeksjon1, templateSeksjon2)
        generator.besvar(3)

        assertEquals(9, søknad.size)
        assertEquals(1, generatorSeksjon.size)
        assertEquals(2, templateSeksjon1.size)
        assertEquals(1, templateSeksjon2.size)
        assertEquals(2, søknad[4].size)
        assertEquals("2.3", søknad[4][1].id)
        assertEquals("3.3", søknad[8][0].id)
    }

    private operator fun Seksjon.get(indeks: Int) = this.sortedBy { it.id }[indeks]
}
