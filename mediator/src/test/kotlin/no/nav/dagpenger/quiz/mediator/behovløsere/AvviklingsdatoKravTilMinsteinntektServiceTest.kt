package no.nav.dagpenger.quiz.mediator.behovløsere

import no.nav.helse.rapids_rivers.asLocalDate
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import java.time.LocalDate

internal class AvviklingsdatoKravTilMinsteinntektServiceTest {
    private val rapid = TestRapid().apply {
        AvviklingsdatoKravTilMinsteinntektService(this)
    }

    @Test
    fun `at vi får 1 april 2022 som dato tilbake`() {
        //language=JSON
        rapid.sendTestMessage(
            """{
          "@behov": [
            "AvviklingsdatoKravTilMinsteinntekt"
          ]
        }
            """.trimIndent()
        )

        with(rapid.inspektør) {
            assertNotNull(field(0, "@løsning"))
            assertEquals(LocalDate.of(2022, 4, 1), field(0, "@løsning")["AvviklingsdatoKravTilMinsteinntekt"].asLocalDate())
        }
    }
}
