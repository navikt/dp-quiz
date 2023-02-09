package no.nav.dagpenger.quiz.mediator.behovløsere

import io.mockk.mockk
import no.nav.dagpenger.quiz.mediator.behovløsere.MetadataStrategi.Metadata
import no.nav.dagpenger.quiz.mediator.db.FaktaRepository
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.util.UUID

internal class MetadataServiceTest {
    val faktaRepository = mockk<FaktaRepository>(relaxed = true)
    val testMetadataStrategi = MetadataStrategi {
        Metadata("04-01.03")
    }
    private val rapid = TestRapid().apply {
        MetadataService(this, faktaRepository, testMetadataStrategi)
    }

    @Test
    fun `at vi får brevkode tilbake`() {
        //language=JSON
        rapid.sendTestMessage(
            """{
          "@event_name": "behov",
          "@behovId": "test123",      
          "@behov": [
            "InnsendingMetadata"
          ],
          "søknad_uuid": "${UUID.randomUUID()}",
          "ident": "12345678913",
          "type": "NY_DIALOG",
          "innsendingId": "999",
          "InnsendingBrevkode": {},
          "@id": "12345",
          "@opprettet": "2022-09-26T09:47:15.296036"
        }
            """.trimIndent(),
        )

        with(rapid.inspektør) {
            Assertions.assertNotNull(field(0, "@løsning"))
            Assertions.assertEquals("04-01.03", field(0, "@løsning")["InnsendingMetadata"]["skjemakode"].asText())
        }
    }
}
