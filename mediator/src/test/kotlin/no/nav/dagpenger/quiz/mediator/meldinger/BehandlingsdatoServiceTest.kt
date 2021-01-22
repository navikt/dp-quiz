package no.nav.dagpenger.quiz.mediator.meldinger

import no.nav.helse.rapids_rivers.asLocalDate
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import java.time.LocalDate

internal class BehandlingsdatoServiceTest {
    private val rapid = TestRapid().apply {
        BehandlingsdatoService(this)
    }

    @Test
    fun `at vi får dagens dato tilbake`() {
        //language=JSON
        rapid.sendTestMessage(
            """{
          "@behov": [
            "Behandlingsdato"
          ]
        }
            """.trimIndent()
        )

        with(rapid.inspektør) {
            assertNotNull(field(0, "@løsning"))
            assertEquals(LocalDate.now(), field(0, "@løsning")["Behandlingsdato"].asLocalDate())
        }
    }
}
