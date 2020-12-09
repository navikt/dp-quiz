package no.nav.dagpenger.model.unit.faktum

import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.heltall
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.ja
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.helpers.SøknadprosessTestBygger
import no.nav.dagpenger.model.helpers.testPerson
import no.nav.dagpenger.model.seksjon.Seksjon
import no.nav.dagpenger.model.seksjon.Søknadprosess
import no.nav.dagpenger.model.seksjon.Versjon.UserInterfaceType.Web
import no.nav.dagpenger.model.subsumsjon.TomSubsumsjon
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class GenerertEndreTest {

    private lateinit var søknadprosess: Søknadprosess

    @BeforeEach
    fun setup() {
        val prototypeSøknad = Søknad(
            59,
            heltall faktum "generator" id 4 genererer 1 og 2 og 3,
            ja nei "template1" id 1,
            ja nei "template2" id 2,
            ja nei "template3" id 3
        )
        val prototypeSøknadprosess = Søknadprosess(
            prototypeSøknad,
            Seksjon("seksjon14", Rolle.søker, prototypeSøknad.ja(1), prototypeSøknad.generator(4)),
            Seksjon("template23", Rolle.søker, prototypeSøknad.ja(2), prototypeSøknad.ja(3))
        )

        søknadprosess = SøknadprosessTestBygger(prototypeSøknad, TomSubsumsjon, mapOf(Web to prototypeSøknadprosess)).søknadprosess(testPerson, Web)
    }

    @Test
    fun ` endre generert faktum `() {
        søknadprosess.generator(4).besvar(3)
        assertEquals(4 + 9, søknadprosess.søknad.size)
        assertEquals(5, søknadprosess.size)

        søknadprosess.generator(4).besvar(2)
        assertEquals(4 + 6, søknadprosess.søknad.size)
        assertEquals(4, søknadprosess.size)

        søknadprosess.generator(4).besvar(0)
        assertEquals(4, søknadprosess.søknad.size)
        assertEquals(2, søknadprosess.size)
    }
}
