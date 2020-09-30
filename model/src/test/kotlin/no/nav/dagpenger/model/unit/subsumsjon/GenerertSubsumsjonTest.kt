package no.nav.dagpenger.model.unit.subsumsjon

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
import java.lang.IllegalArgumentException

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
        val template = FaktumNavn(1, "template").template(Boolean::class.java)
        val generator = FaktumNavn(2, "generator").faktum(Int::class.java, template)
        val makro = "makro template".makro(template er true)
        val subsumsjon = generator med makro
        val root = Søknad(Seksjon(Rolle.søker, generator, template)).let {
            subsumsjon.deepCopy(it)
        }
        generator.besvar(3)
        root.resultat()

        assertEquals(3, (root[0].ugyldig as AlleSubsumsjon).size)
    }
}
