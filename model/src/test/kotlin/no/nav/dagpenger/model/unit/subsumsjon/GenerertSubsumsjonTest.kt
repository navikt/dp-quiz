package no.nav.dagpenger.model.unit.subsumsjon

import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.heltall
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.ja
import no.nav.dagpenger.model.fakta.Fakta
import no.nav.dagpenger.model.fakta.Rolle
import no.nav.dagpenger.model.regel.er
import no.nav.dagpenger.model.regel.med
import no.nav.dagpenger.model.subsumsjon.AlleSubsumsjon
import no.nav.dagpenger.model.subsumsjon.MakroSubsumsjon
import no.nav.dagpenger.model.subsumsjon.makro
import no.nav.dagpenger.model.subsumsjon.så
import no.nav.dagpenger.model.søknad.Seksjon
import no.nav.dagpenger.model.søknad.Søknad
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class GenerertSubsumsjonTest {
    private lateinit var fakta: Fakta

    @BeforeEach
    fun setup() {
        fakta = Fakta(
            ja nei "template" id 1,
            heltall faktum "generator" id 2 genererer 1

        )
    }

    @Test
    fun `Makro template kan ikke ha gyldig ugyldig stier`() {
        val template = fakta ja 1
        val makro = "makro template".makro(template er true) så (template er true)

        assertThrows<IllegalArgumentException> { fakta generator 2 med (makro as MakroSubsumsjon) }
    }

    @Test
    fun `Makro template subsumsjon works`() {

        val makro = "makro template".makro(fakta ja 1 er true)
        val subsumsjon = fakta generator 2 med makro
        val søknad = Søknad(fakta, Seksjon("seksjon", Rolle.søker, fakta generator 2, fakta ja 1), rootSubsumsjon = subsumsjon)

        søknad.generator(2).besvar(3)
        subsumsjon.resultat()

        assertEquals(3, (subsumsjon[0].ugyldig as AlleSubsumsjon).size)
    }
}
