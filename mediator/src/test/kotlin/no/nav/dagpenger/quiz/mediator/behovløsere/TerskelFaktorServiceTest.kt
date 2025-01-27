package no.nav.dagpenger.quiz.mediator.behovløsere

import com.github.navikt.tbd_libs.rapids_and_rivers.test_support.TestRapid
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class TerskelFaktorServiceTest {
    private val rapid =
        TestRapid().apply {
            TerskelFaktorService(this)
        }

    @Test
    fun `at vi får løsning på tersklene`() {
        //language=JSON
        rapid.sendTestMessage(
            """
            {
              "@behov": [
                "ØvreTerskelFaktor",
                "NedreTerskelFaktor"
              ],
              "Virkningstidspunkt": "2020-12-01"
            }
            """.trimIndent(),
        )

        with(rapid.inspektør) {
            Assertions.assertNotNull(field(0, "@løsning"))
            Assertions.assertEquals(3.0, field(0, "@løsning")["ØvreTerskelFaktor"].asDouble())
            Assertions.assertEquals(1.5, field(0, "@løsning")["NedreTerskelFaktor"].asDouble())
        }
    }
}
