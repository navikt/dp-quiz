package no.nav.dagpenger.quiz.mediator.behovløsere

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.dagpenger.quiz.mediator.db.SøknadPersistence
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import java.util.UUID

internal class MigrerProsessServiceTest {
    private val søknadPersistence = mockk<SøknadPersistence>(relaxed = true)
    private val søknadUUID = UUID.randomUUID()
    private val rapid = TestRapid().apply {
        MigrerProsessService(this, søknadPersistence)
    }

    @Test
    fun `besvarer migreringsbehov`() {
        every {
            søknadPersistence.eksisterer(søknadUUID)
        } returns true
        rapid.sendTestMessage( //language=JSON
            """
            {
              "@event_name": "behov",
              "@behovId": "5f9d8703-03f5-483e-983b-170dda083371",
              "@behov": [
                "MigrerProsess"
              ],
              "søknad_uuid": "$søknadUUID",
              "ident": "123123123",
              "@id": "5cde6016-7f38-47fc-9424-01f605498877",
              "@opprettet": "2022-11-10T16:22:27.490863",
              "system_read_count": 0,
              "system_participating_services": [
                {
                  "id": "5cde6016-7f38-47fc-9424-01f605498877",
                  "time": "2022-11-10T16:22:27.490863"
                }
              ]
            }
            """.trimIndent()
        )

        with(rapid.inspektør) {
            assertNotNull(field(0, "@løsning"))
            assertNotNull(field(0, "@løsning")["MigrerProsess"]["prosessnavn"].asText())
            assertNotNull(field(0, "@løsning")["MigrerProsess"]["versjon"].asInt())
            assertFalse(field(0, "@løsning")["MigrerProsess"]["data"].isNull)
        }

        verify(exactly = 1) {
            søknadPersistence.migrer(søknadUUID, any())
            søknadPersistence.hent(søknadUUID)
        }
    }
}
