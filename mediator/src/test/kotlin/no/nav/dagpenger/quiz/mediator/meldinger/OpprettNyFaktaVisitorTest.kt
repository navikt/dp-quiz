package no.nav.dagpenger.quiz.mediator.meldinger

import no.nav.dagpenger.quiz.mediator.helpers.SøknadEksempel
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class OpprettNyFaktaVisitorTest {

    @BeforeEach
    internal fun reset() {
        testRapid.reset()
        søknadPersistance.reset()
    }

    private companion object {
        private val testRapid = TestRapid()
        private val søknadPersistance = UtredningsprosessRepositoryFake()

        init {
            AvslagPåMinsteinntektService(søknadPersistance, testRapid, SøknadEksempel.prosessVersjon)
        }
    }

    @Test
    fun `Start ny søknadprosess, trigget av innsending_ferdigstilt fra dp-mottak`() {
        testRapid.sendTestMessage(innsendingFerdigstiltJson)
        assertEquals(1, testRapid.inspektør.size)
        assertNotNull(søknadPersistance.utredningsprosess)
    }

    @Language("JSON")
    val innsendingFerdigstiltJson =
        """{
          "@id": "3b85fff6-dee8-4ea2-a13b-096b85d8b592",
          "@opprettet": "2021-05-07T11:14:11.502435",
          "journalpostId": "493389306",
          "datoRegistrert": "2021-05-07T11:13:00",
          "type": "NySøknad",
          "fødselsnummer": "12345678910",
          "aktørId": "1000000000000",
          "fagsakId": "237504895",
          "søknadsData": {
            "søknad_uuid": "5d58d882-2d6a-4479-b561-ac60041b6b65"
          },
          "@event_name": "innsending_ferdigstilt"
        }"""
}
