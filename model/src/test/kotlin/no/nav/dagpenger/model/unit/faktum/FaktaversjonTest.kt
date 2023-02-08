package no.nav.dagpenger.model.unit.faktum

import no.nav.dagpenger.model.faktum.Faktaversjon
import no.nav.dagpenger.model.faktum.Prosessnavn
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertContains

internal class FaktaversjonTest {

    private class Navn(override val id: String) : Prosessnavn {
        override fun toString(): String {
            return "Navn{id=$id}"
        }
    }

    @Test
    fun `prosess med likt navn og versjon`() {
        val navnVersjon = Faktaversjon(Navn("A"), 1)

        assertEquals(navnVersjon, navnVersjon)
        assertEquals(navnVersjon.hashCode(), navnVersjon.hashCode())
        assertEquals(Faktaversjon(Navn("A"), 1), Faktaversjon(Navn("A"), 1))
        assertNotEquals(Faktaversjon(Navn("A"), 1), Faktaversjon(Navn("B"), 1))
        assertNotEquals(Faktaversjon(Navn("A"), 1).hashCode(), Faktaversjon(Navn("B"), 1).hashCode())
        assertNotEquals(Faktaversjon(Navn("A"), 1), Faktaversjon(Navn("A"), 2))
        assertNotEquals(Faktaversjon(Navn("A"), 1).hashCode(), Faktaversjon(Navn("A"), 2).hashCode())
    }

    @Test
    fun `Kan ikke ha blankt prosessnavn`() {
        assertThrows<IllegalArgumentException> { Faktaversjon(Navn(""), 1) }
    }

    @Test
    fun `Skal skrive ut relevant info i toString-metoden`() {
        val forventetProsessnavn = "Prosessnavnet"
        val forventetVersjon = 2

        val faktaversjon = Faktaversjon(Navn(forventetProsessnavn), forventetVersjon)

        assertContains(faktaversjon.toString(), forventetProsessnavn)
        assertContains(faktaversjon.toString(), "$forventetVersjon")
    }
}
