package no.nav.dagpenger.model.factory

import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.dato
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.inntekt
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.ja
import no.nav.dagpenger.model.factory.UtledetFaktumFactory.Companion.alle
import no.nav.dagpenger.model.factory.UtledetFaktumFactory.Companion.maks
import no.nav.dagpenger.model.fakta.Inntekt
import no.nav.dagpenger.model.fakta.Inntekt.Companion.årlig
import no.nav.dagpenger.model.fakta.Rolle
import no.nav.dagpenger.model.helpers.januar
import no.nav.dagpenger.model.søknad.Seksjon
import no.nav.dagpenger.model.søknad.Søknad
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.lang.IllegalStateException
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class UtledetFaktumFactoryTest {

    @Test
    fun `maks dato`() {
        val dato1 = (dato faktum "dato1" id 1).faktum
        val dato2 = (dato faktum "dato2" id 2).faktum
        val dato3 = (dato faktum "dato3" id 3).faktum
        val factory = (maks dato "maks dato" av dato1 og dato2 og dato3 id 123).faktum

        val søknad = Søknad(Seksjon("seksjon", Rolle.søker, dato1, dato2, dato3, factory))
        val faktum = søknad.finnFaktum<LocalDate>("123")

        assertFalse(faktum.erBesvart())
        assertThrows<IllegalStateException> { faktum.svar() }
        søknad.finnFaktum<LocalDate>("1").besvar(1.januar)
        søknad.finnFaktum<LocalDate>("2").besvar(2.januar)
        assertFalse(faktum.erBesvart())
        søknad.finnFaktum<LocalDate>("3").besvar(3.januar)
        assertTrue(faktum.erBesvart())
        assertEquals(3.januar, faktum.svar())
        søknad.finnFaktum<LocalDate>("1").besvar(4.januar)
        assertEquals(4.januar, faktum.svar())
    }

    @Test fun `maks inntekt`() {
        val inntekt1 = (inntekt faktum "inntekt1" id 1).faktum
        val inntekt2 = (inntekt faktum "inntekt2" id 2).faktum
        val inntekt3 = (inntekt faktum "inntekt3" id 3).faktum
        val factory = (maks inntekt "maks inntekt" av inntekt1 og inntekt2 og inntekt3 id 123).faktum

        val søknad = Søknad(Seksjon("seksjon", Rolle.søker, inntekt1, inntekt2, inntekt3, factory))
        val faktum = søknad.finnFaktum<LocalDate>("123")

        assertFalse(faktum.erBesvart())
        assertThrows<IllegalStateException> { faktum.svar() }
        søknad.finnFaktum<Inntekt>("1").besvar(260.årlig)
        søknad.finnFaktum<Inntekt>("2").besvar(520.årlig)
        assertFalse(faktum.erBesvart())
        søknad.finnFaktum<Inntekt>("3").besvar(130.årlig)
        assertTrue(faktum.erBesvart())
        assertEquals(520.årlig, faktum.svar())
        søknad.finnFaktum<Inntekt>("1").besvar(1040.årlig)
        assertEquals(1040.årlig, faktum.svar())
    }

    @Test fun `boolean and`() {
        val jaNei1 = (ja nei "jaNei1" id 1).faktum
        val jaNei2 = (ja nei "jaNei2" id 2).faktum
        val jaNei3 = (ja nei "jaNei3" id 3).faktum
        val factory = (alle ja "alle ja" av jaNei1 og jaNei2 og jaNei3 id 123).faktum

        val søknad = Søknad(Seksjon("seksjon", Rolle.søker, jaNei1, jaNei2, jaNei3, factory))
        val faktum = søknad.finnFaktum<LocalDate>("123")

        assertFalse(faktum.erBesvart())
        assertThrows<IllegalStateException> { faktum.svar() }
        søknad.finnFaktum<Boolean>("1").besvar(true)
        søknad.finnFaktum<Boolean>("2").besvar(true)
        assertFalse(faktum.erBesvart())
        søknad.finnFaktum<Boolean>("3").besvar(true)
        assertTrue(faktum.erBesvart())
        assertEquals(true, faktum.svar())
        søknad.finnFaktum<Boolean>("1").besvar(false)
        assertEquals(false, faktum.svar())
    }
}
