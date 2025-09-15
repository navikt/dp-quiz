package no.nav.dagpenger.model.unit.faktum

import no.nav.dagpenger.model.faktum.Tekst
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class TekstTest {
    @Test
    fun `skal kaste feil dersom teksten overskrider grenseverdien`() {
        val tekstMed2000Tegn = "A".repeat(2000)
        assertDoesNotThrow { Tekst(tekstMed2000Tegn) }

        val tekstMed2001Tegn = "A".repeat(2001)
        assertThrows<IllegalArgumentException> { Tekst(tekstMed2001Tegn) }
    }

    @Test
    fun `skal teste likhet`() {
        val tekst = Tekst("tekst")
        assertEquals(tekst, Tekst("tekst"))
        assertEquals(tekst, tekst)
        assertNotEquals(tekst, Tekst("TEKST"))

        val tomTekst = Tekst("")
        assertNotEquals(Any(), tomTekst)
    }
}
