package no.nav.dagpenger.model.unit.factory

import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.boolsk
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.dato
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.inntekt
import no.nav.dagpenger.model.factory.UtledetFaktumFactory.Companion.alle
import no.nav.dagpenger.model.factory.UtledetFaktumFactory.Companion.maks
import no.nav.dagpenger.model.faktum.Inntekt.Companion.årlig
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.helpers.januar
import no.nav.dagpenger.model.helpers.testSøknadprosess
import no.nav.dagpenger.model.helpers.testversjon
import no.nav.dagpenger.model.seksjon.Seksjon
import no.nav.dagpenger.model.seksjon.Søknadprosess
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class UtledetFaktumFactoryTest {

    @Test
    fun `maks dato`() {
        val søknad = Søknad(
            testversjon,
            dato faktum "dato1" id 1,
            dato faktum "dato2" id 2,
            dato faktum "dato3" id 3,
            maks dato "maks dato" av 1 og 2 og 3 id 123
        )

        val søknadprosess = Søknadprosess(
            søknad,
            Seksjon(
                "seksjon",
                Rolle.søker,
                søknad dato 1,
                søknad dato 2,
                søknad dato 3,
                søknad dato 123
            )
        )

        val faktum = søknadprosess.dato("123")

        assertFalse(faktum.erBesvart())
        assertThrows<IllegalStateException> { faktum.svar() }
        søknadprosess.dato("1").besvar(1.januar)
        søknadprosess.dato("2").besvar(2.januar)
        assertFalse(faktum.erBesvart())
        søknadprosess.dato("3").besvar(3.januar)
        assertTrue(faktum.erBesvart())
        assertEquals(3.januar, faktum.svar())
        søknadprosess.dato("1").besvar(4.januar)
        assertEquals(4.januar, faktum.svar())
    }

    @Test
    fun `maks inntekt`() {
        val søknad = Søknad(
            testversjon,
            inntekt faktum "inntekt1" id 1,
            inntekt faktum "inntekt2" id 2,
            inntekt faktum "inntekt3" id 3,
            maks inntekt "maks inntekt" av 1 og 2 og 3 id 123
        )

        val søknadprosess = Søknadprosess(
            søknad,
            Seksjon(
                "seksjon",
                Rolle.søker,
                søknad inntekt 1,
                søknad inntekt 2,
                søknad inntekt 3,
                søknad inntekt 123
            )
        )

        val faktum = søknadprosess.inntekt("123")

        assertFalse(faktum.erBesvart())
        assertThrows<IllegalStateException> { faktum.svar() }
        søknadprosess.inntekt("1").besvar(260.årlig)
        søknadprosess.inntekt("2").besvar(520.årlig)
        assertFalse(faktum.erBesvart())
        søknadprosess.inntekt("3").besvar(130.årlig)
        assertTrue(faktum.erBesvart())
        assertEquals(520.årlig, faktum.svar())
        søknadprosess.inntekt("1").besvar(1040.årlig)
        assertEquals(1040.årlig, faktum.svar())
    }

    @Test
    fun `boolean and`() {
        val søknad = Søknad(
            testversjon,
            boolsk faktum "jaNei1" id 1,
            boolsk faktum "jaNei2" id 2,
            boolsk faktum "jaNei3" id 3,
            alle ja "alle ja" av 1 og 2 og 3 id 123
        )

        val søknadprosess = Søknadprosess(
            søknad,
            Seksjon(
                "seksjon",
                Rolle.søker,
                søknad boolsk 1,
                søknad boolsk 2,
                søknad boolsk 3,
                søknad boolsk 123
            )
        )
        val faktum = søknadprosess.dato("123")

        assertFalse(faktum.erBesvart())
        assertThrows<IllegalStateException> { faktum.svar() }
        søknadprosess.boolsk("1").besvar(true)
        søknadprosess.boolsk("2").besvar(true)
        assertFalse(faktum.erBesvart())
        søknadprosess.boolsk("3").besvar(true)
        assertTrue(faktum.erBesvart())
        assertEquals(true, faktum.svar())
        søknadprosess.boolsk("1").besvar(false)
        assertEquals(false, faktum.svar())
    }

    @Test
    fun `avhengigheter til utledetfaktum`() {

        val søknad = Søknad(
            testversjon,
            dato faktum "dato1" id 1,
            dato faktum "dato2" id 2,
            dato faktum "dato3" id 3,
            maks dato "maks dato" av 1 og 2 og 3 id 123,
            boolsk faktum "boolsk" id 4 avhengerAv 1123,
            maks dato "maks dato 3" av 1 og 123 id 1123
        ).testSøknadprosess()
        søknad.dato(1).besvar(1.januar)
        søknad.dato(2).besvar(2.januar)
        søknad.dato(3).besvar(3.januar)
        assertTrue(søknad.dato(123).erBesvart())
        søknad.boolsk(4).besvar(true)
        assertTrue(søknad.boolsk(4).erBesvart())
        søknad.dato(3).besvar(4.januar)
        assertFalse(søknad.boolsk(4).erBesvart())
    }
}
