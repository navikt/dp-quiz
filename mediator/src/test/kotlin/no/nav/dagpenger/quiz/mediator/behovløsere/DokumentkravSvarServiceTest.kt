package no.nav.dagpenger.quiz.mediator.behovløsere

import com.github.navikt.tbd_libs.rapids_and_rivers.test_support.TestRapid
import io.mockk.mockk
import io.mockk.verify
import no.nav.dagpenger.quiz.mediator.db.ProsessRepository
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.util.UUID

internal class DokumentkravSvarServiceTest {
    private val prosessRepository = mockk<ProsessRepository>(relaxed = true)

    private val rapid =
        TestRapid().apply {
            DokumentkravSvarService(this, prosessRepository)
        }

    @Test
    fun `besvarer dokumentkravSvar`() {
        val søknadUUID = UUID.randomUUID()

        //language=JSON
        rapid.sendTestMessage(
            """
            {
                    "@event_name": "behov",
                    "@behovId": "test123",      
                    "@behov": [
                      "DokumentkravSvar"
                    ],
                    "DokumentkravSvar": {
                      "id": "123",
                      "type": "dokument",
                      "urn": "urn:test:test",
                      "lastOppTidsstempel": "2022-10-26T13:47:56.631795"
            },
                    "søknad_uuid": "$søknadUUID",
                    "ident": "12345678913",
                    "@id": "12345",
                    "@opprettet": "2022-09-26T09:47:15.296036"
                  }
            """.trimIndent(),
        )

        with(rapid.inspektør) {
            Assertions.assertNotNull(field(0, "@løsning"))
            Assertions.assertEquals(søknadUUID.toString(), field(0, "@løsning")["DokumentkravSvar"].asText())
        }

        verify(exactly = 1) {
            prosessRepository.hent(søknadUUID)
            prosessRepository.lagre(any())
        }
    }
}
