package no.nav.dagpenger.model.unit.factory

import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.dato
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.inntekt
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.ja
import no.nav.dagpenger.model.factory.UtledetFaktumFactory.Companion.alle
import no.nav.dagpenger.model.factory.UtledetFaktumFactory.Companion.maks
import no.nav.dagpenger.model.factory.ValgFaktumFactory.Companion.valg
import no.nav.dagpenger.model.faktagrupper.Faktagrupper
import no.nav.dagpenger.model.faktagrupper.Seksjon
import no.nav.dagpenger.model.faktum.Inntekt.Companion.årlig
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.helpers.januar
import no.nav.dagpenger.model.helpers.testFaktagrupper
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class UtledetFaktumFactoryTest {

    @Test
    fun `maks dato`() {
        val søknad = Søknad(
            dato faktum "dato1" id 1,
            dato faktum "dato2" id 2,
            dato faktum "dato3" id 3,
            maks dato "maks dato" av 1 og 2 og 3 id 123
        )

        val faktagrupper = Faktagrupper(
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
        val søknad = Søknad(
            inntekt faktum "inntekt1" id 1,
            inntekt faktum "inntekt2" id 2,
            inntekt faktum "inntekt3" id 3,
            maks inntekt "maks inntekt" av 1 og 2 og 3 id 123
        )

        val faktagrupper = Faktagrupper(
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
        val søknad = Søknad(
            ja nei "jaNei1" id 1,
            ja nei "jaNei2" id 2,
            ja nei "jaNei3" id 3,
            alle ja "alle ja" av 1 og 2 og 3 id 123
        )

        val faktagrupper = Faktagrupper(
            søknad,
            Seksjon(
                "seksjon",
                Rolle.søker,
                søknad ja 1,
                søknad ja 2,
                søknad ja 3,
                søknad ja 123
            )
        )
        val faktum = faktagrupper.dato("123")

        assertFalse(faktum.erBesvart())
        assertThrows<IllegalStateException> { faktum.svar() }
        faktagrupper.ja("1").besvar(true)
        faktagrupper.ja("2").besvar(true)
        assertFalse(faktum.erBesvart())
        faktagrupper.ja("3").besvar(true)
        assertTrue(faktum.erBesvart())
        assertEquals(true, faktum.svar())
        faktagrupper.ja("1").besvar(false)
        assertEquals(false, faktum.svar())
    }

    @Test
    fun `En eller ingen`() {
        val søknad = Søknad(
            valg faktum "valg" ja "valg1" ja "valg2" nei "valg3" id 1
        ).testFaktagrupper()
        assertFalse(søknad.ja(2).erBesvart())
        assertFalse(søknad.ja(3).erBesvart())
        assertFalse(søknad.ja(4).erBesvart())
        assertFalse(søknad.ja(1).erBesvart())
        søknad.ja(2).besvar(true, Rolle.søker)
        assertTrue(søknad.ja(2).erBesvart())
        assertTrue(søknad.ja(1).svar())
        søknad.ja(4).besvar(true, Rolle.søker)
        assertTrue(søknad.ja(4).erBesvart())
        assertFalse(søknad.ja(2).erBesvart())
        assertFalse(søknad.ja(1).svar())
    }
}
