package no.nav.dagpenger.quiz.mediator.behovløsere

import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

internal class SenesteMuligeVirkningsdatoServiceTest {
    private val rapid = TestRapid().apply {
        SenesteMuligeVirkningsdatoService(this)
    }

    @Test
    fun `at vi får dagens dato tilbake`() {
        //language=JSON
        rapid.sendTestMessage(
            """{
          "@behov": [
            "SenesteMuligeVirkningstidspunkt"
          ],
          "Behandlingsdato": "2021-01-01"
        }
            """.trimIndent()
        )

        with(rapid.inspektør) {
            assertNotNull(field(0, "@løsning"))
            assertEquals("2021-01-15", field(0, "@løsning")["SenesteMuligeVirkningstidspunkt"].asText())
        }
    }
}
