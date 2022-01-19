package no.nav.dagpenger.model.faktum

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import java.time.LocalDate
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class PeriodeTest {

    private val første = LocalDate.of(2020, 10, 10)
    private val siste = LocalDate.of(2022, 1, 1)

    @Test
    fun `Skal kunne opprette en gyldig periode`() {
        val gyldigPeriode = Periode(første, siste)
        assertDoesNotThrow { gyldigPeriode }
        assertFalse(gyldigPeriode.erPågående())
    }

    @Test
    fun `Fom kan ikke være etter tom`() {
        assertThrows<IllegalArgumentException> { Periode(siste, første) }
        assertDoesNotThrow { Periode(første, første) }
    }

    @Test
    fun `Skal kunne opprette en pågående periode`() {
        assertDoesNotThrow { Periode(LocalDate.now().minusMonths(2)) }
        val pågåendePeriode = Periode(LocalDate.now().minusMonths(5))
        assertTrue(pågåendePeriode.erPågående())
    }
}
