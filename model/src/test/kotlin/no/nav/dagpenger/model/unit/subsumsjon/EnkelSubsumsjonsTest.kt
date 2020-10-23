package no.nav.dagpenger.model.unit.subsumsjon

import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.dato
import no.nav.dagpenger.model.fakta.Fakta
import no.nav.dagpenger.model.helpers.januar
import no.nav.dagpenger.model.helpers.testSøknad
import no.nav.dagpenger.model.regel.etter
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class EnkelSubsumsjonsTest {

    val fakta = Fakta(
        dato faktum "Datoen du fyller 67" id 1,
        dato faktum "Datoen du søker om dagpenger" id 2
    ).testSøknad()

    val bursdag67 = fakta dato 1
    val søknadsdato = fakta dato 2

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
}
