package no.nav.dagpenger.model.factory

import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.dato
import no.nav.dagpenger.model.fakta.Rolle
import no.nav.dagpenger.model.factory.UtledetFaktumFactory.Companion.maks
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
}
