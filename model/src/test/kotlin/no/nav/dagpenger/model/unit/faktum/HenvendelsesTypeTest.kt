package no.nav.dagpenger.model.unit.faktum

import no.nav.dagpenger.model.faktum.HenvendelsesType
import no.nav.dagpenger.model.faktum.Prosessnavn
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertContains

internal class HenvendelsesTypeTest {

    private class Navn(override val id: String) : Prosessnavn {
        override fun toString(): String {
            return "Navn{id=$id}"
        }
    }

    @Test
    fun `prosess med likt navn og versjon`() {
        val navnVersjon = HenvendelsesType(Navn("A"), 1)

        assertEquals(navnVersjon, navnVersjon)
        assertEquals(navnVersjon.hashCode(), navnVersjon.hashCode())
        assertEquals(HenvendelsesType(Navn("A"), 1), HenvendelsesType(Navn("A"), 1))
        assertNotEquals(HenvendelsesType(Navn("A"), 1), HenvendelsesType(Navn("B"), 1))
        assertNotEquals(HenvendelsesType(Navn("A"), 1).hashCode(), HenvendelsesType(Navn("B"), 1).hashCode())
        assertNotEquals(HenvendelsesType(Navn("A"), 1), HenvendelsesType(Navn("A"), 2))
        assertNotEquals(HenvendelsesType(Navn("A"), 1).hashCode(), HenvendelsesType(Navn("A"), 2).hashCode())
    }

    @Test
    fun `Kan ikke ha blankt prosessnavn`() {
        assertThrows<IllegalArgumentException> { HenvendelsesType(Navn(""), 1) }
    }

    @Test
    fun `Skal skrive ut relevant info i toString-metoden`() {
        val forventetProsessnavn = "Prosessnavnet"
        val forventetVersjon = 2

        val henvendelsesType = HenvendelsesType(Navn(forventetProsessnavn), forventetVersjon)

        assertContains(henvendelsesType.toString(), forventetProsessnavn)
        assertContains(henvendelsesType.toString(), "$forventetVersjon")
    }
}
