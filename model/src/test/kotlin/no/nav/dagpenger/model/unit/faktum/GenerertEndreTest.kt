package no.nav.dagpenger.model.unit.faktum

import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.boolsk
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.heltall
import no.nav.dagpenger.model.faktum.Fakta
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.helpers.testPerson
import no.nav.dagpenger.model.helpers.testversjon
import no.nav.dagpenger.model.seksjon.Faktagrupper
import no.nav.dagpenger.model.seksjon.Seksjon
import no.nav.dagpenger.model.seksjon.Versjon
import no.nav.dagpenger.model.subsumsjon.TomSubsumsjon
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class GenerertEndreTest {

    private lateinit var faktagrupper: Faktagrupper

    @BeforeEach
    fun setup() {
        val prototypeFakta = Fakta(
            testversjon,
            heltall faktum "generator" id 4 genererer 1 og 2 og 3,
            boolsk faktum "template1" id 1,
            boolsk faktum "template2" id 2,
            boolsk faktum "template3" id 3
        )
        val prototypeFaktagrupper = Faktagrupper(
            prototypeFakta,
            Seksjon("seksjon14", Rolle.søker, prototypeFakta.boolsk(1), prototypeFakta.generator(4)),
            Seksjon("template23", Rolle.søker, prototypeFakta.boolsk(2), prototypeFakta.boolsk(3))
        )

        faktagrupper = Versjon.Bygger(
            prototypeFakta,
            TomSubsumsjon,
            prototypeFaktagrupper
        ).søknadprosess(testPerson)
    }

    @Test
    fun ` endre generert faktum `() {
        faktagrupper.generator(4).besvar(3)
        assertEquals(4 + 9, faktagrupper.fakta.size)
        assertEquals(5, faktagrupper.size)

        faktagrupper.generator(4).besvar(2)
        assertEquals(4 + 6, faktagrupper.fakta.size)
        assertEquals(4, faktagrupper.size)

        faktagrupper.generator(4).besvar(0)
        assertEquals(4, faktagrupper.fakta.size)
        assertEquals(2, faktagrupper.size)
    }
}
