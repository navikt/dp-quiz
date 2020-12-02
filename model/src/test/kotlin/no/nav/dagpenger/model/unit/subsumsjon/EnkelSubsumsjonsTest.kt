package no.nav.dagpenger.model.unit.subsumsjon

import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.dato
import no.nav.dagpenger.model.faktum.Faktum
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.helpers.januar
import no.nav.dagpenger.model.helpers.testSøknadprosess
import no.nav.dagpenger.model.regel.etter
import no.nav.dagpenger.model.seksjon.Søknadprosess
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDate

internal class EnkelSubsumsjonsTest {
    private lateinit var søknadprosess: Søknadprosess
    private lateinit var bursdag67: Faktum<LocalDate>
    private lateinit var søknadsdato: Faktum<LocalDate>

    companion object {
        private var versjonId = 120
    }

    @BeforeEach
    fun setup() {
        versjonId--
        søknadprosess = Søknad(
            versjonId,
            dato faktum "Datoen du fyller 67" id 1,
            dato faktum "Datoen du søker om dagpenger" id 2
        ).testSøknadprosess()
        bursdag67 = søknadprosess dato 1
        søknadsdato = søknadprosess dato 2
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
}
