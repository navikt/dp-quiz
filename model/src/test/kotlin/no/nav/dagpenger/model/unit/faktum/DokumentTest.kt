package no.nav.dagpenger.model.unit.faktum

import de.slub.urn.URN
import no.nav.dagpenger.model.faktum.Dokument
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import java.time.LocalDateTime

class DokumentTest {
    private val now = LocalDateTime.now()

    @Test
    fun initializtion() {
        assertDoesNotThrow {
            Dokument(now, "urn:nid:sse")
            Dokument(now, URN.rfc8141().parse("urn:nid:sse"))
        }

        assertThrows<IllegalArgumentException> {
            Dokument(now, "hubba:nid:sse")
            Dokument(now, URN.rfc2141().parse("urn:nid:sse"))
        }
    }

    @Test
    fun `equality test`() {
        val document = Dokument(now, "urn:sid:sse")

        assertEquals(document, document)
        assertEquals(document, Dokument(now, "urn:sid:sse"))
        assertEquals(Dokument(now, "urn:sid:sse"), document)
        assertNotEquals(document, Dokument(LocalDateTime.now(), "urn:sid:sse1"))
        assertNotEquals(document, Any())
        assertNotEquals(document, null)
    }
}
