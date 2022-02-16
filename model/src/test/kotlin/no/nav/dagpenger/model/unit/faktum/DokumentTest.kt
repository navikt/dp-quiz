package no.nav.dagpenger.model.unit.faktum

import no.nav.dagpenger.model.faktum.Dokument
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class DokumentTest {

    private val nå = LocalDateTime.now()
    private val urn = "urn:sid:sse"
    private val document = Dokument(nå, urn)

    @Test
    fun `equality test`() {
        assertEquals(document, document)
        assertEquals(document, Dokument(nå, urn))
        assertEquals(Dokument(nå, urn), document)
        assertNotEquals(document, Dokument(LocalDateTime.now(), "urn:sid:sse1"))
        assertNotEquals(document, Any())
        assertNotEquals(document, null)
    }
}
