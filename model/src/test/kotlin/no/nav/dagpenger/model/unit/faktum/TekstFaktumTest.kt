package no.nav.dagpenger.model.unit.faktum

import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.tekst
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.faktum.Tekst
import no.nav.dagpenger.model.helpers.testSøknadprosess
import no.nav.dagpenger.model.helpers.testversjon
import no.nav.dagpenger.model.seksjon.Faktagrupper
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

    lateinit var søknad: Faktagrupper

    @BeforeEach
    fun setup() {
        søknad = prototypeSøknad.testSøknadprosess(TomSubsumsjon)
    }

    @Test
    fun `Skal kunne besvare et tekstfaktum`() {
        val svartekst1 = Tekst("første svar")
        val tekstFaktum = søknad.tekst(1)
        assertDoesNotThrow { tekstFaktum.besvar(svartekst1) }
        assertTrue { tekstFaktum.erBesvart() }
        assertEquals(svartekst1, tekstFaktum.svar())

        val svartekst2 = Tekst("nytt svar")
        tekstFaktum.besvar(svartekst2)
        assertTrue { tekstFaktum.erBesvart() }
        assertEquals(svartekst2, tekstFaktum.svar())
    }
}
