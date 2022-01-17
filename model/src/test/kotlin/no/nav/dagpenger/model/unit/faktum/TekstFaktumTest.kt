package no.nav.dagpenger.model.unit.faktum

import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.tekst
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.faktum.Tekst
import no.nav.dagpenger.model.helpers.testSøknadprosess
import no.nav.dagpenger.model.helpers.testversjon
import no.nav.dagpenger.model.seksjon.Søknadprosess
import no.nav.dagpenger.model.subsumsjon.TomSubsumsjon
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TekstFaktumTest {

    val prototypeSøknad = Søknad(
        testversjon,
        tekst faktum "tekst" id 1
    )

    lateinit var søknad: Søknadprosess

    @BeforeEach
    fun setup() {
        søknad = prototypeSøknad.testSøknadprosess(TomSubsumsjon)
    }

    @Test
    fun `Skal kunne besvare et tekstfaktum`() {
        val tekst = Tekst("tekst1")
        val tekstFaktum = søknad.tekst(1)
        assertDoesNotThrow { tekstFaktum.besvar(tekst) }
        assertTrue { tekstFaktum.erBesvart() }
        assertEquals(tekst, tekstFaktum.svar())
        assertEquals(Tekst("tekst1"), tekst)
    }
}
