package no.nav.dagpenger.model.unit.faktum

import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.dato
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.heltall
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.helpers.februar
import no.nav.dagpenger.model.helpers.januar
import no.nav.dagpenger.model.helpers.testPerson
import no.nav.dagpenger.model.regel.har
import no.nav.dagpenger.model.regel.mellom
import no.nav.dagpenger.model.seksjon.Seksjon
import no.nav.dagpenger.model.seksjon.Søknadprosess
import no.nav.dagpenger.model.seksjon.Versjon
import no.nav.dagpenger.model.seksjon.Versjon.UserInterfaceType.Web
import no.nav.dagpenger.model.subsumsjon.makro
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PeriodeGeneratorTest {

    private val versjonId by lazy { kotlin.runCatching { Versjon.siste }.getOrDefault(0) }
    val søknadPrototype = Søknad(
        versjonId,
        heltall faktum "periode antall" id 1 genererer 2 og 3,
        dato faktum "fom" id 2,
        dato faktum "tom" id 3,
        dato faktum "ønsket dato" id 4
    )
    private val subsumsjon = søknadPrototype generator 1 har "periode".makro(
        søknadPrototype.dato(4) mellom søknadPrototype.dato(2) og søknadPrototype.dato(3)
    )

    private val søknadprosess = Søknadprosess(
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

    init {
        Versjon(søknadPrototype, subsumsjon, mapOf(Web to søknadprosess))
    }

    @Test
    fun ` periode faktum `() {
        val søknadprosess = Versjon.id(versjonId).søknadprosess(testPerson, Web)
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
}
