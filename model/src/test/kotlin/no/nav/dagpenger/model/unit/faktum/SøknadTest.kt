package no.nav.dagpenger.model.unit.faktum

import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.dato
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.dokument
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.heltall
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.ja
import no.nav.dagpenger.model.factory.UtledetFaktumFactory.Companion.maks
import no.nav.dagpenger.model.faktagrupper.Faktagrupper
import no.nav.dagpenger.model.faktagrupper.Seksjon
import no.nav.dagpenger.model.faktum.Dokument
import no.nav.dagpenger.model.faktum.GeneratorFaktum
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.faktum.TemplateFaktum
import no.nav.dagpenger.model.helpers.januar
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

class SøknadTest {

    @Test
    fun `søknad med ett faktum`() {
        val søknad = Søknad(ja nei "janei" id 3)
        assertFalse((søknad id 3).erBesvart())
        assertFalse((søknad id "3").erBesvart())
        assertEquals(1, søknad.size)
        assertThrows<IllegalArgumentException> { søknad id 1 }
    }

    @Test
    fun `fakta med avhengigheter`() {
        val søknad = Søknad(
            dokument faktum "f11" id 11,
            ja nei "f12" id 12 avhengerAv 11
        )

        Faktagrupper(Seksjon("seksjon", Rolle.søker, søknad id 11, søknad id 12))
        assertEquals(2, søknad.size)
        søknad.dokument(11).besvar(Dokument(1.januar), Rolle.søker)
        søknad.ja(12).besvar(true, Rolle.søker)

        assertTrue(søknad.ja(12).erBesvart())
        søknad.dokument(11).besvar(Dokument(2.januar), Rolle.søker)
        assertFalse(søknad.ja(12).erBesvart())
    }

    @Test
    fun `sammensatte fakta`() {
        val søknad = Søknad(
            dato faktum "f3" id 3,
            dato faktum "f4" id 4,
            dato faktum "f5" id 5,
            maks dato "maksdato" av 3 og 4 og 5 id 345
        )

        Faktagrupper(Seksjon("seksjon", Rolle.søker, søknad id 345, søknad id 3, søknad id 4, søknad id 5))
        assertEquals(4, søknad.size)
        søknad.dato(3).besvar(3.januar)
        søknad.dato(4).besvar(4.januar)
        assertFalse(søknad.id(345).erBesvart())
        søknad.dato(5).besvar(5.januar)
        assertTrue(søknad.id(345).erBesvart())
        assertEquals(5.januar, søknad.id(345).svar())
        søknad.dato(3).besvar(30.januar)
        assertEquals(30.januar, søknad.id(345).svar())
    }

    @Test
    fun `fakta templater `() {
        val søknad = Søknad(
            heltall faktum "antall barn" id 15 genererer 16 og 17 og 18,
            heltall faktum "alder barn" id 16,
            ja nei "skal du ha penger for barn" id 17,
            ja nei "annen forelder får støtte" id 18
        )
        val barneSeksjon = Seksjon("barneseksjon", Rolle.søker, søknad id 15, søknad id 16, søknad id 17, søknad id 18)
        Faktagrupper(søknad, barneSeksjon)
        assertEquals(TemplateFaktum::class, søknad.id(16)::class)
        assertEquals(GeneratorFaktum::class, søknad.id(15)::class)
        assertEquals(4, søknad.size)
        (søknad generator 15).besvar(2, Rolle.søker)
        assertEquals(10, søknad.size)
        assertIder(søknad, "16.1", "16.2", "17.1", "17.2", "18.1", "18.2", "16", "17", "18", "15")
    }

    @Test
    fun `sortere utledede faktum`() {
        val søknad = Søknad(
            dato faktum "f3" id 3,
            dato faktum "f4" id 4,
            dato faktum "f5" id 5,
            maks dato "maksdato" av 6 og 7 id 8,
            maks dato "maksdato" av 5 og 7 id 6,
            maks dato "maksdato" av 3 og 4 id 7,
        )

        assertIder(søknad, 3, 4, 5, 7, 6, 8)
    }

    @Test
    fun `Søknad med duplikate ider `() {
        assertThrows<IllegalArgumentException> {
            Søknad(
                heltall faktum "f11" id 11,
                dokument faktum "whoops" id 11,
            )
        }
    }

    private fun assertIder(søknad: Søknad, vararg ider: Int) {
        assertEquals(ider.map { it.toString() }, søknad.map { it.id })
    }

    private fun assertIder(søknad: Søknad, vararg ider: String) {
        assertEquals(ider.toList(), søknad.map { it.id })
    }
}
