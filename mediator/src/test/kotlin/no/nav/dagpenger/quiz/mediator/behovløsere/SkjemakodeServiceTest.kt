package no.nav.dagpenger.quiz.mediator.behovløsere

import io.mockk.mockk
import no.nav.dagpenger.quiz.mediator.db.SøknadPersistence
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.util.UUID

internal class SkjemakodeServiceTest {

    val søknadPersistence = mockk<SøknadPersistence>(relaxed = true)

    private val rapid = TestRapid().apply {
        SkjemakodeService(this, søknadPersistence)
    }

    @Test
    fun `at vi får brevkode tilbake`() {
        //language=JSON
        rapid.sendTestMessage(
            """{
          "@event_name": "behov",
          "@behovId": "test123",      
          "@behov": [
            "Skjemakode"
          ],
          "søknad_uuid": "${UUID.randomUUID()}",
          "ident": "12345678913",
          "type": "NY_DIALOG",
          "innsendingId": "999",
          "InnsendingBrevkode": {},
          "@id": "12345",
          "@opprettet": "2022-09-26T09:47:15.296036"
        }
            """.trimIndent()
        )

        with(rapid.inspektør) {
            Assertions.assertNotNull(field(0, "@løsning"))
            Assertions.assertEquals("Søknad om dagpenger (ikke permittert)", field(0, "@løsning")["Skjemakode"]["tittel"].asText())
            Assertions.assertEquals("04-01.03", field(0, "@løsning")["Skjemakode"]["skjemakode"].asText())
        }
    }
}
