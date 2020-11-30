package no.nav.dagpenger.model.unit.subsumsjon

import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.dato
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.periode
import no.nav.dagpenger.model.faktum.Faktum
import no.nav.dagpenger.model.faktum.Periode
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.faktum.til
import no.nav.dagpenger.model.helpers.desember
import no.nav.dagpenger.model.helpers.januar
import no.nav.dagpenger.model.helpers.november
import no.nav.dagpenger.model.helpers.testSøknadprosess
import no.nav.dagpenger.model.regel.etter
import no.nav.dagpenger.model.regel.innenfor
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
    private lateinit var registretPeriode: Faktum<Periode>

    companion object {
        private var versjonId = 120
    }

    @BeforeEach
    fun setup() {
        versjonId--
        søknadprosess = Søknad(
            versjonId,
            dato faktum "Datoen du fyller 67" id 1,
            dato faktum "Datoen du søker om dagpenger" id 2,
            periode faktum "Fegistrert fom" id 3
        ).testSøknadprosess()
        bursdag67 = søknadprosess dato 1
        søknadsdato = søknadprosess dato 2
        registretPeriode = søknadprosess periode 3
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
    fun `dato innenfor periode subsumsjon`() {
        assertEquals(null, (søknadsdato innenfor registretPeriode).resultat())
        søknadsdato.besvar(1.desember)
        registretPeriode.besvar(15.november til 3.desember)
        assertTrue((søknadsdato innenfor registretPeriode).resultat()!!)
        søknadsdato.besvar(4.desember)
        assertFalse((søknadsdato innenfor registretPeriode).resultat()!!)
    }
}
