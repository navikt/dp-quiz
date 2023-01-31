package no.nav.dagpenger.model.unit.factory

import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.boolsk
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.dato
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.inntekt
import no.nav.dagpenger.model.factory.UtledetFaktumFactory.Companion.alle
import no.nav.dagpenger.model.factory.UtledetFaktumFactory.Companion.grensedato67år
import no.nav.dagpenger.model.factory.UtledetFaktumFactory.Companion.maks
import no.nav.dagpenger.model.faktum.Fakta
import no.nav.dagpenger.model.faktum.Inntekt.Companion.årlig
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.helpers.februar
import no.nav.dagpenger.model.helpers.januar
import no.nav.dagpenger.model.helpers.mars
import no.nav.dagpenger.model.helpers.testSøknadprosess
import no.nav.dagpenger.model.helpers.testversjon
import no.nav.dagpenger.model.seksjon.Faktagrupper
import no.nav.dagpenger.model.seksjon.Seksjon
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class UtledetFaktumFactoryTest {

    @Test
    fun `maks dato`() {
        val fakta = Fakta(
            testversjon,
            dato faktum "dato1" id 1,
            dato faktum "dato2" id 2,
            dato faktum "dato3" id 3,
            maks dato "maks dato" av 1 og 2 og 3 id 123
        )

        val faktagrupper = Faktagrupper(
            fakta,
            Seksjon(
                "seksjon",
                Rolle.søker,
                fakta dato 1,
                fakta dato 2,
                fakta dato 3,
                fakta dato 123
            )
        )

        val faktum = faktagrupper.dato("123")

        assertFalse(faktum.erBesvart())
        assertThrows<IllegalStateException> { faktum.svar() }
        faktagrupper.dato("1").besvar(1.januar)
        faktagrupper.dato("2").besvar(2.januar)
        assertFalse(faktum.erBesvart())
        faktagrupper.dato("3").besvar(3.januar)
        assertTrue(faktum.erBesvart())
        assertEquals(3.januar, faktum.svar())
        faktagrupper.dato("1").besvar(4.januar)
        assertEquals(4.januar, faktum.svar())
    }

    @Test
    fun `maks inntekt`() {
        val fakta = Fakta(
            testversjon,
            inntekt faktum "inntekt1" id 1,
            inntekt faktum "inntekt2" id 2,
            inntekt faktum "inntekt3" id 3,
            maks inntekt "maks inntekt" av 1 og 2 og 3 id 123
        )

        val faktagrupper = Faktagrupper(
            fakta,
            Seksjon(
                "seksjon",
                Rolle.søker,
                fakta inntekt 1,
                fakta inntekt 2,
                fakta inntekt 3,
                fakta inntekt 123
            )
        )

        val faktum = faktagrupper.inntekt("123")

        assertFalse(faktum.erBesvart())
        assertThrows<IllegalStateException> { faktum.svar() }
        faktagrupper.inntekt("1").besvar(260.årlig)
        faktagrupper.inntekt("2").besvar(520.årlig)
        assertFalse(faktum.erBesvart())
        faktagrupper.inntekt("3").besvar(130.årlig)
        assertTrue(faktum.erBesvart())
        assertEquals(520.årlig, faktum.svar())
        faktagrupper.inntekt("1").besvar(1040.årlig)
        assertEquals(1040.årlig, faktum.svar())
    }

    @Test
    fun `boolean and`() {
        val fakta = Fakta(
            testversjon,
            boolsk faktum "jaNei1" id 1,
            boolsk faktum "jaNei2" id 2,
            boolsk faktum "jaNei3" id 3,
            alle ja "alle ja" av 1 og 2 og 3 id 123
        )

        val faktagrupper = Faktagrupper(
            fakta,
            Seksjon(
                "seksjon",
                Rolle.søker,
                fakta boolsk 1,
                fakta boolsk 2,
                fakta boolsk 3,
                fakta boolsk 123
            )
        )
        val faktum = faktagrupper.dato("123")

        assertFalse(faktum.erBesvart())
        assertThrows<IllegalStateException> { faktum.svar() }
        faktagrupper.boolsk("1").besvar(true)
        faktagrupper.boolsk("2").besvar(true)
        assertFalse(faktum.erBesvart())
        faktagrupper.boolsk("3").besvar(true)
        assertTrue(faktum.erBesvart())
        assertEquals(true, faktum.svar())
        faktagrupper.boolsk("1").besvar(false)
        assertEquals(false, faktum.svar())
    }

    @Test
    fun `avhengigheter til utledetfaktum`() {

        val fakta = Fakta(
            testversjon,
            dato faktum "dato1" id 1,
            dato faktum "dato2" id 2,
            dato faktum "dato3" id 3,
            maks dato "maks dato" av 1 og 2 og 3 id 123,
            boolsk faktum "boolsk" id 4 avhengerAv 1123,
            maks dato "maks dato 3" av 1 og 123 id 1123
        ).testSøknadprosess()
        fakta.dato(1).besvar(1.januar)
        fakta.dato(2).besvar(2.januar)
        fakta.dato(3).besvar(3.januar)
        assertTrue(fakta.dato(123).erBesvart())
        fakta.boolsk(4).besvar(true)
        assertTrue(fakta.boolsk(4).erBesvart())
        fakta.dato(3).besvar(4.januar)
        assertFalse(fakta.boolsk(4).erBesvart())
    }

    @Test
    fun `grensedato for 67 år`() {
        val fakta = Fakta(
            testversjon,
            dato faktum "dato1" id 1,
            grensedato67år dato "utledetDato" av 1 id 2
        ).testSøknadprosess()

        fakta.dato(1).besvar(1.januar(1950))
        assertEquals(1.februar(2017), fakta.id(2).svar())

        fakta.dato(1).besvar(31.januar(1950))
        assertEquals(1.februar(2017), fakta.id(2).svar())

        fakta.dato(1).besvar(1.februar(1950))
        assertEquals(1.mars(2017), fakta.id(2).svar())
    }
}
