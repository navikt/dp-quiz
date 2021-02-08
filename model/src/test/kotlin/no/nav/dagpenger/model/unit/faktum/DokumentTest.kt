package no.nav.dagpenger.model.unit.faktum

import no.nav.dagpenger.model.faktum.Dokument
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class DokumentTest {

    private val nå = LocalDateTime.now()
    private val url = "https://localhost"
    private val document = Dokument(nå, url)

    @Test
    fun `equality test`() {
        assertEquals(document, document)
        assertEquals(document, Dokument(nå, url))
        assertEquals(Dokument(nå, url), document)
        assertNotEquals(document, Any())
        assertNotEquals(document, null)
    }
}
