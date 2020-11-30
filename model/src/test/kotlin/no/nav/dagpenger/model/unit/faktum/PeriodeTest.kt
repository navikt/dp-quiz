package no.nav.dagpenger.model.unit.faktum

import no.nav.dagpenger.model.faktum.Periode
import no.nav.dagpenger.model.faktum.i
import no.nav.dagpenger.model.helpers.august
import no.nav.dagpenger.model.helpers.februar
import no.nav.dagpenger.model.helpers.juli
import no.nav.dagpenger.model.helpers.juni
import no.nav.dagpenger.model.helpers.september
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class PeriodeTest {
    val periode = Periode(11.juni, 12.juli)

    @Test
    fun `Lager en periode`() {
        assertEquals(periode, Periode(11.juni, 12.juli))
        assertThrows<IllegalArgumentException> {
            Periode(4.september, 28.august)
        }

        assertThrows<IllegalArgumentException> {
            Periode(4.september, 4.september)
        }
    }

    @Test
    fun `sjekker om dato er i periode`() {
        assertTrue(18.juni i periode)
        assertFalse(18.februar i periode)
        assertFalse(10.juni i periode || 13.juli i periode)
    }
}
