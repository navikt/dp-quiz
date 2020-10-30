package no.nav.dagpenger.model.unit.factory

import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.dato
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.inntekt
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.ja
import no.nav.dagpenger.model.factory.UtledetFaktumFactory.Companion.alle
import no.nav.dagpenger.model.factory.UtledetFaktumFactory.Companion.maks
import no.nav.dagpenger.model.factory.UtledetFaktumFactory.Companion.valg
import no.nav.dagpenger.model.fakta.Fakta
import no.nav.dagpenger.model.fakta.Inntekt.Companion.årlig
import no.nav.dagpenger.model.fakta.Rolle
import no.nav.dagpenger.model.helpers.januar
import no.nav.dagpenger.model.søknad.Faktagrupper
import no.nav.dagpenger.model.søknad.Seksjon
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class UtledetFaktumFactoryTest {

    @Test
    fun `maks dato`() {
        val fakta = Fakta(
            dato faktum "dato1" id 1,
            dato faktum "dato2" id 2,
            dato faktum "dato3" id 3,
            maks dato "maks dato" av 1 og 2 og 3 id 123
        )

        val søknad = Faktagrupper(
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

        val faktum = søknad.dato("123")

        assertFalse(faktum.erBesvart())
        assertThrows<IllegalStateException> { faktum.svar() }
        søknad.dato("1").besvar(1.januar)
        søknad.dato("2").besvar(2.januar)
        assertFalse(faktum.erBesvart())
        søknad.dato("3").besvar(3.januar)
        assertTrue(faktum.erBesvart())
        assertEquals(3.januar, faktum.svar())
        søknad.dato("1").besvar(4.januar)
        assertEquals(4.januar, faktum.svar())
    }

    @Test fun `maks inntekt`() {
        val fakta = Fakta(
            inntekt faktum "inntekt1" id 1,
            inntekt faktum "inntekt2" id 2,
            inntekt faktum "inntekt3" id 3,
            maks inntekt "maks inntekt" av 1 og 2 og 3 id 123
        )

        val søknad = Faktagrupper(
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

        val faktum = søknad.inntekt("123")

        assertFalse(faktum.erBesvart())
        assertThrows<IllegalStateException> { faktum.svar() }
        søknad.inntekt("1").besvar(260.årlig)
        søknad.inntekt("2").besvar(520.årlig)
        assertFalse(faktum.erBesvart())
        søknad.inntekt("3").besvar(130.årlig)
        assertTrue(faktum.erBesvart())
        assertEquals(520.årlig, faktum.svar())
        søknad.inntekt("1").besvar(1040.årlig)
        assertEquals(1040.årlig, faktum.svar())
    }

    @Test fun `boolean and`() {
        val fakta = Fakta(
            ja nei "jaNei1" id 1,
            ja nei "jaNei2" id 2,
            ja nei "jaNei3" id 3,
            alle ja "alle ja" av 1 og 2 og 3 id 123
        )

        val søknad = Faktagrupper(
            fakta,
            Seksjon(
                "seksjon",
                Rolle.søker,
                fakta ja 1,
                fakta ja 2,
                fakta ja 3,
                fakta ja 123
            )
        )
        val faktum = søknad.dato("123")

        assertFalse(faktum.erBesvart())
        assertThrows<IllegalStateException> { faktum.svar() }
        søknad.ja("1").besvar(true)
        søknad.ja("2").besvar(true)
        assertFalse(faktum.erBesvart())
        søknad.ja("3").besvar(true)
        assertTrue(faktum.erBesvart())
        assertEquals(true, faktum.svar())
        søknad.ja("1").besvar(false)
        assertEquals(false, faktum.svar())
    }

    @Test fun `En eller ingen`() {
        val fakta = Fakta(
            valg faktum "valg" ja "valg1" ja "valg2" nei "valg3" id 1
        )
    }
}
