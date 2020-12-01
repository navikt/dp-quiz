package no.nav.dagpenger.model.unit.subsumsjon

import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.dato
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.heltall
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.ja
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.periode
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.faktum.til
import no.nav.dagpenger.model.helpers.februar
import no.nav.dagpenger.model.helpers.mars
import no.nav.dagpenger.model.regel.er
import no.nav.dagpenger.model.regel.har
import no.nav.dagpenger.model.regel.innenfor
import no.nav.dagpenger.model.regel.med
import no.nav.dagpenger.model.seksjon.Seksjon
import no.nav.dagpenger.model.seksjon.Søknadprosess
import no.nav.dagpenger.model.seksjon.Versjon
import no.nav.dagpenger.model.subsumsjon.AlleSubsumsjon
import no.nav.dagpenger.model.subsumsjon.MakroSubsumsjon
import no.nav.dagpenger.model.subsumsjon.makro
import no.nav.dagpenger.model.subsumsjon.så
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertFalse

internal class GenerertSubsumsjonTest {
    private lateinit var søknad: Søknad

    companion object {
        private var versjonId = runCatching { Versjon.siste }.getOrDefault(0)
    }

    @BeforeEach
    fun setup() {
        søknad = Søknad(
            versjonId++,
            ja nei "template" id 1,
            heltall faktum "generator" id 2 genererer 1,
            periode faktum "periode template" id 3,
            heltall faktum "antall perioder" id 4 genererer 3,
            dato faktum "innenfor faktum" id 5

        )
    }

    @Test
    fun `Makro template kan ikke ha gyldig ugyldig stier`() {
        val template = søknad ja 1
        val makro = "makro template".makro(template er true) så (template er true)

        assertThrows<IllegalArgumentException> { søknad generator 2 med (makro as MakroSubsumsjon) }
    }

    @Test
    fun `Makro template subsumsjon works`() {

        val makro = "makro template".makro(søknad ja 1 er true)
        val subsumsjon = søknad generator 2 med makro
        val søknadprosess = Søknadprosess(søknad, Seksjon("seksjon", Rolle.søker, søknad generator 2, søknad ja 1), rootSubsumsjon = subsumsjon)

        søknadprosess.generator(2).besvar(3)
        subsumsjon.resultat()

        assertEquals(3, (subsumsjon[0].ugyldig as AlleSubsumsjon).size)

        søknadprosess.generator(2).besvar(2)
        subsumsjon.resultat()

        assertEquals(2, (subsumsjon[0].ugyldig as AlleSubsumsjon).size)
    }

    @Test
    fun `minst en av på genererte perioder`() {
        val skalVæreInnenfor = søknad.dato(5)
        val periode = søknad.periode(3)
        val generator = søknad.generator(4)

        val innenforPeriode = "innenfor periode".makro(
            skalVæreInnenfor innenfor periode
        )
        val subsumsjon = generator har innenforPeriode

        val søknadprosess = Søknadprosess(
            søknad,
            Seksjon("fra bruker", Rolle.søker, skalVæreInnenfor),
            Seksjon("fra nav", Rolle.nav, periode),
            rootSubsumsjon = subsumsjon
        )
        søknadprosess.dato(5).besvar(3.februar)
        søknadprosess.generator(4).besvar(3)
        søknadprosess.periode("3.1").besvar(1.februar til 5.februar)
        assertEquals(2, søknadprosess.nesteFakta().size)
        assertNull(søknadprosess.resultat())

        søknadprosess.periode("3.2").besvar(1.mars til 5.mars)
        søknadprosess.periode("3.3").besvar(13.februar til 20.februar)
        assertEquals(0, søknadprosess.nesteFakta().size)
        assertTrue(søknadprosess.resultat()!!)

        søknadprosess.periode("3.1").besvar(22.februar til 3.mars)
        assertFalse(søknadprosess.resultat()!!)
    }
}
