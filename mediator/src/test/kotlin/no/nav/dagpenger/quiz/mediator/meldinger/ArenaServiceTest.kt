package no.nav.dagpenger.quiz.mediator.meldinger

import io.mockk.every
import io.mockk.mockk
import no.nav.dagpenger.model.seksjon.Søknadprosess
import no.nav.dagpenger.quiz.mediator.db.SøknadPersistence
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.UUID

internal class ArenaServiceTest {
    internal val testRapid = TestRapid()
    internal val testPersistence: SøknadPersistence = mockk(relaxed = true)
    internal val søknad_uuid = UUID.randomUUID()

    @BeforeEach()
    fun setup() {
        ArenaService(testPersistence, testRapid)
        testRapid.reset()
    }

    @Test
    fun `sender arena melding`() {
        testRapid.sendTestMessage(prosessFerdigMelding(søknad_uuid))
        every { testPersistence.hent(any(), any()) } returns Søknadprosess()
        assertEquals(søknad_uuid.toString(), testRapid.inspektør.field(0, "søknad_uuid").asText())
        assertEquals("arena_vedtak", testRapid.inspektør.field(0, "@event_name").asText())
    }
}

@Language("json")
private fun prosessFerdigMelding(uuid: UUID) =
    """{
    "@event_name": "prosess_ferdig",
    "søknad_uuid": "$uuid"
}""".trimMargin()
