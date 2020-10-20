package no.nav.dagpenger.model.unit.fakta

import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.dato
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.dokument
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.ja
import no.nav.dagpenger.model.factory.UtledetFaktumFactory.Companion.maks
import no.nav.dagpenger.model.fakta.Dokument
import no.nav.dagpenger.model.fakta.Fakta
import no.nav.dagpenger.model.fakta.Rolle
import no.nav.dagpenger.model.helpers.januar
import no.nav.dagpenger.model.søknad.Seksjon
import no.nav.dagpenger.model.søknad.Søknad
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

class FaktaTest {

    @Test
    fun `fakta med ett faktum`() {
        val fakta = Fakta("12345678910", ja nei "janei" id 3)
        assertFalse((fakta id 3).erBesvart())
        assertFalse((fakta id "3").erBesvart())
        assertThrows<IllegalArgumentException> { fakta id 1 }
    }

    @Test
    fun `fakta med avhengigheter`() {
        val fakta = Fakta(
            "12345678910",
            dokument faktum "f11" id 11,
            ja nei "f12" id 12 avhengerAv 11
        )

        Søknad(Seksjon("seksjon", Rolle.søker, fakta id 11, fakta id 12))
        fakta.dokument(11).besvar(Dokument(1.januar), Rolle.søker)
        fakta.ja(12).besvar(true, Rolle.søker)

        assertTrue(fakta.ja(12).erBesvart())
        fakta.dokument(11).besvar(Dokument(2.januar), Rolle.søker)
        assertFalse(fakta.ja(12).erBesvart())
    }

    @Test
    fun `sammensatte fakta`() {
        val fakta = Fakta(
            "12345678910",
            dato faktum "f3" id 3,
            dato faktum "f4" id 4,
            dato faktum "f5" id 5,
            maks dato "maksdato" av 3 og 4 og 5 id 345
        )

        Søknad(Seksjon("seksjon", Rolle.søker, fakta id 345, fakta id 3, fakta id 4, fakta id 5))
        fakta.dato(3).besvar(3.januar)
        fakta.dato(4).besvar(4.januar)
        assertFalse(fakta.id(345).erBesvart())
        fakta.dato(5).besvar(5.januar)
        assertTrue(fakta.id(345).erBesvart())
        assertEquals(5.januar, fakta.id(345).svar())
        fakta.dato(3).besvar(30.januar)
        assertEquals(30.januar, fakta.id(345).svar())
    }
}
