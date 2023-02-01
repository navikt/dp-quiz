package no.nav.dagpenger.model.unit.faktum

import no.nav.dagpenger.model.faktum.FaktaVersjon
import no.nav.dagpenger.model.faktum.HenvendelsesType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertContains

internal class FaktaVersjonTest {

    private class Navn(override val id: String) : HenvendelsesType {
        override fun toString(): String {
            return "Navn{id=$id}"
        }
    }

    @Test
    fun `prosess med likt navn og versjon`() {
        val navnVersjon = FaktaVersjon(Navn("A"), 1)

        assertEquals(navnVersjon, navnVersjon)
        assertEquals(navnVersjon.hashCode(), navnVersjon.hashCode())
        assertEquals(FaktaVersjon(Navn("A"), 1), FaktaVersjon(Navn("A"), 1))
        assertNotEquals(FaktaVersjon(Navn("A"), 1), FaktaVersjon(Navn("B"), 1))
        assertNotEquals(FaktaVersjon(Navn("A"), 1).hashCode(), FaktaVersjon(Navn("B"), 1).hashCode())
        assertNotEquals(FaktaVersjon(Navn("A"), 1), FaktaVersjon(Navn("A"), 2))
        assertNotEquals(FaktaVersjon(Navn("A"), 1).hashCode(), FaktaVersjon(Navn("A"), 2).hashCode())
    }

    @Test
    fun `Kan ikke ha blankt prosessnavn`() {
        assertThrows<IllegalArgumentException> { FaktaVersjon(Navn(""), 1) }
    }

    @Test
    fun `Skal skrive ut relevant info i toString-metoden`() {
        val forventetProsessnavn = "Prosessnavnet"
        val forventetVersjon = 2

        val faktaVersjon = FaktaVersjon(Navn(forventetProsessnavn), forventetVersjon)

        assertContains(faktaVersjon.toString(), forventetProsessnavn)
        assertContains(faktaVersjon.toString(), "$forventetVersjon")
    }
}
