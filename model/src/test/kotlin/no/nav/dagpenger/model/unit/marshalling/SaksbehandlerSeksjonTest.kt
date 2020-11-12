package no.nav.dagpenger.model.unit.marshalling

import no.nav.dagpenger.model.helpers.NyttEksempel
import no.nav.dagpenger.model.marshalling.SaksbehandlerJsonBuilder
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class SaksbehandlerSeksjonTest {

    @Test
    fun `Komplekse seksjoner`() {
        assertSeksjonSize(8, "seksjon8")
        assertSeksjonSize(5, "seksjon4")
        assertSeksjonSize(5, "seksjon2")
    }

    @Test
    fun `Genererte seksjoner kan bli sendt`() {
        val fakta = NyttEksempel().faktagrupper
        fakta.heltall(15).besvar(3)
        var json = SaksbehandlerJsonBuilder(fakta, "seksjon8").resultat()
        assertEquals(11, json["fakta"].size())
        json = SaksbehandlerJsonBuilder(fakta, "seksjon7", 1).resultat()
        assertEquals(1, json["fakta"].size())
    }

    private fun assertSeksjonSize(expected: Int, seksjonNavn: String) {
        val json = SaksbehandlerJsonBuilder(NyttEksempel().faktagrupper, seksjonNavn).resultat()
        assertEquals(expected, json["fakta"].size())
    }
}
