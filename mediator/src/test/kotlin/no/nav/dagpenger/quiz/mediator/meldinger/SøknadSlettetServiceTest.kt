package no.nav.dagpenger.quiz.mediator.meldinger

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.dagpenger.quiz.mediator.db.SøknadPersistence
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import java.lang.Exception
import java.util.UUID

internal class SøknadSlettetServiceTest {

    val søknadUUIDfeilerIkke = UUID.randomUUID()
    val søknadUUIDfeiler = UUID.randomUUID()
    val søknadPersistence = mockk<SøknadPersistence>(relaxed = true).also {
        every { it.slett(søknadUUIDfeiler) } throws Exception("Noe gikk galt under sletting")
        every { it.slett(søknadUUIDfeilerIkke) } returns true
    }
    val testRapid = TestRapid().also {
        SøknadSlettetService(
            rapidsConnection = it,
            søknadPersistence = søknadPersistence
        )
    }

    @Test
    fun `tar i mot søknadSlettetEvent`() {
        testRapid.sendTestMessage(søknadSlettetEvent(søknadUUIDfeilerIkke))
        verify {
            søknadPersistence.slett(søknadUUIDfeilerIkke)
        }
    }

    @Test
    fun `Appen krasjer ikke hvis sletting går galt`() {
        assertDoesNotThrow {
            testRapid.sendTestMessage(søknadSlettetEvent(søknadUUIDfeiler))
            verify {
                søknadPersistence.slett(søknadUUIDfeiler)
            }
        }
    }

    fun søknadSlettetEvent(søknadUUID: UUID) =
        //language=JSON
        """{
  "@event_name": "søknad_slettet",
  "@opprettet": "2022-06-24T14:26:43.975947",
  "@id": "190de272-9535-40c6-b710-6d233cbdd600",
  "søknad_uuid": "$søknadUUID",
  "system_read_count": 0,
  "system_participating_services": [
    {
      "id": "190de272-9535-40c6-b710-6d233cbdd600",
      "time": "2022-06-24T14:26:44.026033"
    }
  ]
}
        """.trimMargin()
}
