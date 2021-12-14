package no.nav.dagpenger.quiz.mediator.meldinger

import io.getunleash.FakeUnleash
import no.nav.dagpenger.quiz.mediator.helpers.SøknadEksempel
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class NySøknadTest {

    @BeforeEach
    internal fun reset() {
        testRapid.reset()
        søknadPersistance.reset()
    }

    private companion object {
        private val testRapid = TestRapid()
        private val søknadPersistance = SøknadPersistenceFake()
        private val unleash = FakeUnleash().also {
            it.enableAll()
        }

        init {
            AvslagPåMinsteinntektService(søknadPersistance, testRapid, unleash, SøknadEksempel.prosessVersjon)
        }
    }

    @Test
    fun `Start ny søknadprosess, trigget av innsending_ferdigstilt fra dp-mottak`() {
        testRapid.sendTestMessage(innsendingFerdigstiltJson)
        assertEquals(1, testRapid.inspektør.size)
        assertNotNull(søknadPersistance.søknadprosess)
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
            "soknadId": 11724,
            "soknadsType": "dagpenger.ordinaer",
            "skjemaNummer": "NAV 04-01.03",
            "versjon": null,
            "uuid": "b529c315-0176-4554-a41a-925f89c94079",
            "brukerBehandlingId": "10010WQMW",
            "behandlingskjedeId": null,
            "fakta": [],
            "status": "UNDER_ARBEID",
            "aktoerId": "12345678910",
            "opprettetDato": "2021-05-03T11:14:22.362Z",
            "sistLagret": "2021-05-07T09:13:37.004Z",
            "delstegStatus": "VEDLEGG_VALIDERT",
            "vedlegg": [],
            "journalforendeEnhet": null,
            "soknadPrefix": "dagpenger.ordinaer",
            "soknadUrl": "/soknaddagpenger/NAV04-01.03",
            "fortsettSoknadUrl": "/soknaddagpenger/utslagskriterier",
            "erEttersending": false
          },
          "@event_name": "innsending_ferdigstilt"
        }"""
}
