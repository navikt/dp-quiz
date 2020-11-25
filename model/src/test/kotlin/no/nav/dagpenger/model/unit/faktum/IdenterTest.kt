package no.nav.dagpenger.model.unit.faktum

import no.nav.dagpenger.model.faktum.Identer
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class IdenterTest {

    @Test
    fun `noe`() {
        val ident1 = Identer.Builder().folkeregisterIdent("12345678919").aktørId("12345").build()
        val ident2 = Identer.Builder().aktørId("12345").folkeregisterIdent("12345678919").build()
        val ident3 = Identer.Builder().folkeregisterIdent("12345").build()

        assertEquals(ident1, ident2)
        assertEquals(Identer(setOf()), Identer(setOf()))
        assertNotEquals(ident1, ident3)
        assertNotEquals(Identer(setOf()), null)
        assertNotEquals(Identer(setOf()), Any())
    }
}
