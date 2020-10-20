package no.nav.dagpenger.model.unit.fakta

import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.dokument
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.ja
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
            ja nei "f12" id 12 avhengerAv 11,
        )

        Søknad(Seksjon("seksjon", Rolle.søker, fakta id 11, fakta id 12))
        fakta.dokument(11).besvar(Dokument(1.januar), Rolle.søker)
        fakta.ja(12).besvar(true, Rolle.søker)

        assertTrue(fakta.ja(12).erBesvart())
        fakta.dokument(11).besvar(Dokument(2.januar), Rolle.søker)
        assertFalse(fakta.ja(12).erBesvart())
    }
}
