package no.nav.dagpenger.quiz.mediator.meldinger

import io.mockk.mockk
import io.mockk.verify
import no.nav.dagpenger.quiz.mediator.db.SøknadPersistence
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.junit.jupiter.api.Test

class SøknadSlettetServiceTest {

    val søknadPersistence = mockk<SøknadPersistence>()
    val testRapid = TestRapid().also {
        SøknadSlettetService(
            rapidsConnection = it,
            søknadPersistence = søknadPersistence
        )
    }

    @Test
    fun `tar i mot søknadSlettetEvent`() {
        testRapid.publish(søknadSlettetEvent())
        verify {
            søknadPersistence.slett(any())
        }
    }

    fun søknadSlettetEvent() =
        //language=JSON
        """{
  "@event_name": "søknad_slettet",
  "@opprettet": "2022-06-24T14:26:43.975947",
  "@id": "190de272-9535-40c6-b710-6d233cbdd600",
  "søknad_uuid": "d955e6bf-9a3e-4266-a071-0aa8bef12cdc",
  "system_read_count": 0,
  "system_participating_services": [
    {
      "id": "190de272-9535-40c6-b710-6d233cbdd600",
      "time": "2022-06-24T14:26:44.026033"
    }
  ]
}""".trimMargin()
}