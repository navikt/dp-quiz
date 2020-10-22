package no.nav.dagpenger.model.unit.subsumsjon

import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.heltall
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.ja
import no.nav.dagpenger.model.fakta.Fakta
import no.nav.dagpenger.model.fakta.FaktumNavn
import no.nav.dagpenger.model.fakta.Rolle
import no.nav.dagpenger.model.fakta.faktum
import no.nav.dagpenger.model.fakta.template
import no.nav.dagpenger.model.regel.er
import no.nav.dagpenger.model.regel.med
import no.nav.dagpenger.model.subsumsjon.AlleSubsumsjon
import no.nav.dagpenger.model.subsumsjon.MakroSubsumsjon
import no.nav.dagpenger.model.subsumsjon.makro
import no.nav.dagpenger.model.subsumsjon.så
import no.nav.dagpenger.model.søknad.Seksjon
import no.nav.dagpenger.model.søknad.Søknad
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class GenerertSubsumsjonTest {

    @Test
    fun `Makro template kan ikke ha gyldig ugyldig stier`() {
        val template = FaktumNavn(1, "template").template(Boolean::class.java)
        val generator = FaktumNavn(2, "generator").faktum(Int::class.java, template)
        val makro = "makro template".makro(template er true) så (template er true)

        assertThrows<IllegalArgumentException> { generator med (makro as MakroSubsumsjon) }
    }

    @Test
    fun `Makro template subsumsjon works`() {
        val fakta = Fakta(
                ja nei "template" id 1,
                heltall faktum "generator" id 2 genererer 1

        )
        val makro = "makro template".makro(fakta ja 1 er true)
        val subsumsjon = fakta generator 2 med makro
        val søknad = Søknad(fakta, subsumsjon, Seksjon("seksjon", Rolle.søker, fakta generator 2, fakta ja 1))

        søknad.generator(2).besvar(3)
        subsumsjon.resultat()

        assertEquals(3, (subsumsjon[0].ugyldig as AlleSubsumsjon).size)
    }
}
