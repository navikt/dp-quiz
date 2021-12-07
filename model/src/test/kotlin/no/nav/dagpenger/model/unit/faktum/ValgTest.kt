package no.nav.dagpenger.model.unit.faktum

import no.nav.dagpenger.model.faktum.Valg
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class ValgTest {
    @Test
    fun `sjekke likhet`(){
        val valg1 = Valg(listOf("valg1", "valg2"))
        val valg2 = Valg(listOf("valg3", "valg4"))

        assertEquals(valg1, valg1)
        assertNotEquals(valg1, Any())
        assertNotEquals(valg1, valg2)
        assertEquals(valg1.hashCode(), valg1.hashCode())
        assertNotEquals(valg1.hashCode(), valg2.hashCode())
    }
}