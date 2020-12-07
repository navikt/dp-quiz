package no.nav.dagpenger.quiz.mediator.meldinger

import no.nav.helse.rapids_rivers.asLocalDate
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import java.time.LocalDate

internal class DagensDatoServiceTest {
    private val rapid = TestRapid().apply {
        DagensDatoService(this)
    }

    @Test
    fun `at vi får dagens dato tilbake`() {
        //language=JSON
        rapid.sendTestMessage(
            """{
          "@behov": [
            "DagensDato"
          ]
        }
            """.trimIndent()
        )

        with(rapid.inspektør) {
            assertNotNull(field(0, "@løsning"))
            assertEquals(LocalDate.now(), field(0, "@løsning")["DagensDato"].asLocalDate())
        }
    }
}
