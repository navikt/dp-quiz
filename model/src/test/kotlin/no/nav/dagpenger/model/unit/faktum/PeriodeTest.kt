package no.nav.dagpenger.model.unit.faktum

import no.nav.dagpenger.model.faktum.Periode
import no.nav.dagpenger.model.helpers.januar
import no.nav.dagpenger.model.helpers.mars
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class PeriodeTest {

    @Test
    fun `Skal kunne opprette en gyldig periode`() {
        val periodeMedSluttdato = assertDoesNotThrow { Periode(1.januar(), 15.mars()) }
        assertFalse(periodeMedSluttdato.erPågående())
    }

    @Test
    fun `Fom kan ikke være etter tom`() {
        assertThrows<IllegalArgumentException> { Periode(15.mars(), 1.januar()) }
        assertDoesNotThrow { Periode(1.januar(), 1.januar()) }
    }

    @Test
    fun `Skal kunne opprette en pågående periode`() {
        val periodeUtenSluttdato = assertDoesNotThrow { Periode(19.januar()) }
        assertTrue(periodeUtenSluttdato.erPågående())
    }

    @Test
    fun `Skal teste likhet`() {
        val gyldigPeriode = Periode(1.januar(), 15.mars())
        assertEquals(gyldigPeriode, gyldigPeriode)

        val gyldigPeriodeNyInstans = Periode(1.januar(), 15.mars())
        assertEquals(gyldigPeriode, gyldigPeriodeNyInstans)

        assertEquals(gyldigPeriode.hashCode(), gyldigPeriodeNyInstans.hashCode())
    }
}
