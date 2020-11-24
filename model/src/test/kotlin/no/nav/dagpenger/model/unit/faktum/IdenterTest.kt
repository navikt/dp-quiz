package no.nav.dagpenger.model.unit.faktum

import no.nav.dagpenger.model.faktum.Identer
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class IdenterTest {

    @Test
    fun `noe`() {
        val ident1 = Identer().folkeregisterIdent("12345678919").aktørId("12345")
        val ident2 = Identer().aktørId("12345").folkeregisterIdent("12345678919")
        val ident3 = Identer().folkeregisterIdent("12345")

        assertEquals(ident1, ident2)
        assertEquals(Identer(), Identer())
        assertNotEquals(ident1, ident3)
        assertNotEquals(Identer(), null)
        assertNotEquals(Identer(), Any())
    }
}
