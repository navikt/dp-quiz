package no.nav.dagpenger.model.unit.faktum

import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.dato
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.heltall
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.helpers.desember
import no.nav.dagpenger.model.helpers.februar
import no.nav.dagpenger.model.helpers.januar
import no.nav.dagpenger.model.helpers.testPerson
import no.nav.dagpenger.model.regel.har
import no.nav.dagpenger.model.regel.mellom
import no.nav.dagpenger.model.seksjon.Seksjon
import no.nav.dagpenger.model.seksjon.Søknadprosess
import no.nav.dagpenger.model.seksjon.Versjon
import no.nav.dagpenger.model.seksjon.Versjon.UserInterfaceType.Web
import no.nav.dagpenger.model.subsumsjon.deltre
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class GeneratorFaktumTest {

    private lateinit var søknadprosessTestBygger: Versjon.Bygger

    @BeforeEach
    fun setup() {
        val søknadPrototype = Søknad(
            0,
            heltall faktum "periode antall" id 1 genererer 2 og 3,
            dato faktum "fom" id 2,
            dato faktum "tom" id 3,
            dato faktum "ønsket dato" id 4
        )
        val prototypeSubsumsjon = søknadPrototype generator 1 har "periode".deltre {
            søknadPrototype.dato(4) mellom søknadPrototype.dato(2) og søknadPrototype.dato(3)
        }

        val prototypeSøknadprosess = Søknadprosess(
            Seksjon(
                "periode antall",
                Rolle.nav,
                søknadPrototype generator 1
            ),
            Seksjon(
                "periode",
                Rolle.nav,
                søknadPrototype dato 2,
                søknadPrototype dato 3,
            ),
            Seksjon(
                "søknadsdato",
                Rolle.søker,
                søknadPrototype dato 4,
            ),
        )
        søknadprosessTestBygger = Versjon.Bygger(søknadPrototype, prototypeSubsumsjon, mapOf(Web to prototypeSøknadprosess))
    }

    @Test
    fun ` periode faktum `() {
        val søknadprosess = søknadprosessTestBygger.søknadprosess(testPerson, Web)
        søknadprosess.generator(1).besvar(2)
        søknadprosess.dato(4).besvar(5.januar)
        søknadprosess.dato("2.1").besvar(1.januar)
        søknadprosess.dato("3.1").besvar(8.januar)
        søknadprosess.dato("2.2").besvar(1.februar)
        søknadprosess.dato("3.2").besvar(8.februar)

        assertEquals(5, søknadprosess.size)
        assertTrue(søknadprosess.rootSubsumsjon.resultat()!!)
        søknadprosess.dato(4).besvar(12.januar)
        assertFalse(søknadprosess.rootSubsumsjon.resultat()!!)

        søknadprosess.dato(4).besvar(8.januar)
        assertTrue(søknadprosess.rootSubsumsjon.resultat()!!)
    }

    @Test
    fun ` har med tom generator blir false `() {
        val søknadprosess = søknadprosessTestBygger.søknadprosess(testPerson, Web)
        søknadprosess.generator(1).besvar(0)
        assertEquals(false, søknadprosess.rootSubsumsjon.resultat())
    }

    @Test
    fun ` har med en gyldig generert subsumsjon blir true `() {
        val søknadprosess = søknadprosessTestBygger.søknadprosess(testPerson, Web)
        søknadprosess.generator(1).besvar(1)
        søknadprosess.dato("2.1").besvar(1.januar)
        søknadprosess.dato("3.1").besvar(8.januar)
        søknadprosess.dato(4).besvar(5.januar)
        assertEquals(true, søknadprosess.rootSubsumsjon.resultat())
    }

    @Test
    fun ` har med en ugyldig generert subsumsjon blir false `() {
        val søknadprosess = søknadprosessTestBygger.søknadprosess(testPerson, Web)
        søknadprosess.generator(1).besvar(1)
        søknadprosess.dato("2.1").besvar(1.desember)
        søknadprosess.dato("3.1").besvar(8.desember)
        søknadprosess.dato(4).besvar(5.januar)
        assertEquals(false, søknadprosess.rootSubsumsjon.resultat())
    }

    @Test
    fun ` har med gyldig og ugyldig genererte subsumsjoner blir true `() {
        val søknadprosess = søknadprosessTestBygger.søknadprosess(testPerson, Web)
        søknadprosess.generator(1).besvar(2)
        søknadprosess.dato("2.1").besvar(1.desember)
        søknadprosess.dato("3.1").besvar(8.desember)
        søknadprosess.dato("2.2").besvar(1.januar)
        søknadprosess.dato("3.2").besvar(8.januar)
        søknadprosess.dato(4).besvar(5.januar)
        assertEquals(true, søknadprosess.rootSubsumsjon.resultat())
    }
}
