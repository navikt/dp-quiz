package no.nav.dagpenger.model.unit.faktum

import no.nav.dagpenger.model.faktum.Prosessnavn
import no.nav.dagpenger.model.faktum.Prosessversjon
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class ProsessversjonTest {

    private class Navn(override val id: String) : Prosessnavn

    @Test
    fun `prosess med likt navn og versjon`() {
        val navnVersjon = Prosessversjon(Navn("A"), 1)

        assertEquals(navnVersjon, navnVersjon)
        assertEquals(navnVersjon.hashCode(), navnVersjon.hashCode())
        assertEquals(Prosessversjon(Navn("A"), 1), Prosessversjon(Navn("A"), 1))
        assertNotEquals(Prosessversjon(Navn("A"), 1), Prosessversjon(Navn("B"), 1))
        assertNotEquals(Prosessversjon(Navn("A"), 1).hashCode(), Prosessversjon(Navn("B"), 1).hashCode())
        assertNotEquals(Prosessversjon(Navn("A"), 1), Prosessversjon(Navn("A"), 2))
        assertNotEquals(Prosessversjon(Navn("A"), 1).hashCode(), Prosessversjon(Navn("A"), 2).hashCode())
    }

    @Test
    fun `Kan ikke ha blankt prosessnavn`() {
        assertThrows<IllegalArgumentException> { Prosessversjon(Navn(""), 1) }
    }
}
