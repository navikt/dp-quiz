package no.nav.dagpenger.quiz.mediator.behovløsere

import io.mockk.mockk
import io.mockk.verify
import no.nav.dagpenger.quiz.mediator.db.SøknadPersistence
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.util.UUID

internal class MigrerProsessServiceTest {
    val søknadPersistence = mockk<SøknadPersistence>(relaxed = true)

    private val rapid = TestRapid().apply {
        MigrerProsessService(this, søknadPersistence)
    }

    @Test
    fun `besvarer migreringsbehov`() {
        val søknadUUID = UUID.randomUUID()

        //language=JSON
        rapid.sendTestMessage(
            """{
          "@event_name": "behov",
          "@behovId": "test123",      
          "@behov": [
            "MigrerProsess"
          ],
          "søknad_uuid": "$søknadUUID",
          "ident": "12345678913",
          "@id": "12345",
          "@opprettet": "2022-09-26T09:47:15.296036"
        }
            """.trimIndent()
        )

        with(rapid.inspektør) {
            Assertions.assertNotNull(field(0, "@løsning"))
            Assertions.assertEquals(søknadUUID.toString(), field(0, "@løsning")["MigrerProsess"].asText())
        }

        verify(exactly = 1) {
            søknadPersistence.migrer(søknadUUID)
        }
    }
}
