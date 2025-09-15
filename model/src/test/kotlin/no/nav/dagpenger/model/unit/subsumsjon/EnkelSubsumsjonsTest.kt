package no.nav.dagpenger.model.unit.subsumsjon

import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.boolsk
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.dato
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.heltall
import no.nav.dagpenger.model.faktum.Fakta
import no.nav.dagpenger.model.faktum.Faktum
import no.nav.dagpenger.model.helpers.januar
import no.nav.dagpenger.model.helpers.testSøknadprosess
import no.nav.dagpenger.model.helpers.testversjon
import no.nav.dagpenger.model.regel.er
import no.nav.dagpenger.model.regel.etter
import no.nav.dagpenger.model.regel.minst
import no.nav.dagpenger.model.regel.utfylt
import no.nav.dagpenger.model.seksjon.Prosess
import no.nav.dagpenger.model.subsumsjon.hvisIkkeOppfylt
import no.nav.dagpenger.model.subsumsjon.hvisOppfylt
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDate

internal class EnkelSubsumsjonsTest {
    private lateinit var prosess: Prosess
    private lateinit var bursdag67: Faktum<LocalDate>
    private lateinit var søknadsdato: Faktum<LocalDate>
    private lateinit var etValg: Faktum<Boolean>

    @BeforeEach
    fun setup() {
        prosess =
            Fakta(
                testversjon,
                dato faktum "Datoen du fyller 67" id 1,
                dato faktum "Datoen du søker om dagpenger" id 2,
                boolsk faktum "Et valg" id 3,
            ).testSøknadprosess()
        bursdag67 = prosess dato 1
        søknadsdato = prosess dato 2
        etValg = prosess boolsk 3
    }

    @Test
    fun `subsumsjonen kan konkludere`() {
        assertEquals(null, (bursdag67 etter søknadsdato).resultat())
        bursdag67.besvar(31.januar)
        søknadsdato.besvar(1.januar)
        assertTrue((bursdag67 etter søknadsdato).resultat()!!)
    }

    @Test
    fun `subsumsjonen kan konkludere negativt`() {
        bursdag67.besvar(1.januar)
        søknadsdato.besvar(31.januar)
        assertFalse((bursdag67 etter søknadsdato).resultat()!!)
    }

    @Test
    fun `skal ikke gå å overskrive oppfylt og ikke oppfylt subsumsjon`() {
        val subsumsjon = bursdag67 etter søknadsdato hvisOppfylt { etValg.utfylt() } hvisIkkeOppfylt { etValg er false }

        assertThrows<IllegalArgumentException> {
            subsumsjon hvisOppfylt { etValg er true }
        }

        assertThrows<IllegalArgumentException> {
            subsumsjon hvisIkkeOppfylt { etValg er true }
        }
    }

    @Test
    fun `heltall minst regel`() {
        val søknadprosess = Fakta(testversjon, heltall faktum "heltall" id 1).testSøknadprosess()
        val heltall = søknadprosess.heltall(1)
        val minstSubsumsumsjon = heltall minst 2
        heltall.besvar(1)
        assertFalse(minstSubsumsumsjon.resultat()!!)
        heltall.besvar(2)
        assertTrue(minstSubsumsumsjon.resultat()!!)
    }
}
