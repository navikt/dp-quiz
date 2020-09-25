package no.nav.dagpenger.model.unit.subsumsjon

import no.nav.dagpenger.model.fakta.Rolle
import no.nav.dagpenger.model.fakta.faktum
import no.nav.dagpenger.model.helpers.DATOEN_DU_FYLLER_67
import no.nav.dagpenger.model.helpers.DATOEN_DU_SØKER_OM_DAGPENGER
import no.nav.dagpenger.model.helpers.januar
import no.nav.dagpenger.model.regel.etter
import no.nav.dagpenger.model.søknad.Seksjon
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.LocalDate

internal class EnkelSubsumsjonsTest {

    val bursdag67 = DATOEN_DU_FYLLER_67.faktum<LocalDate>(LocalDate::class.java)
    val søknadsdato = DATOEN_DU_SØKER_OM_DAGPENGER.faktum<LocalDate>(LocalDate::class.java)
    val seksjon = Seksjon(Rolle.søker, bursdag67, søknadsdato)

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
