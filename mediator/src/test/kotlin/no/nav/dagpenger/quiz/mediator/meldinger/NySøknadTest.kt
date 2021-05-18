package no.nav.dagpenger.quiz.mediator.meldinger

import no.finn.unleash.FakeUnleash
import no.nav.dagpenger.quiz.mediator.FEATURE_MOTTA_SØKNAD
import no.nav.dagpenger.quiz.mediator.helpers.SøknadEksempel
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.UUID

internal class NySøknadTest {

    @BeforeEach
    internal fun reset() {
        testRapid.reset()
        søknadPersistance.reset()
    }

    private companion object {
        private val testRapid = TestRapid()
        private val søknadPersistance = SøknadPersistenceFake()
        private val fakeUnleash = FakeUnleash()

        init {
            NySøknadService(søknadPersistance, testRapid, SøknadEksempel.versjonId)
            MottattSøknadService(søknadPersistance, testRapid, fakeUnleash, SøknadEksempel.versjonId)
        }
    }

    @Test
    fun `Start ny søknad, og send første seksjon`() {
        testRapid.sendTestMessage(nySøknadMelding())
        assertEquals(1, testRapid.inspektør.size)
        assertNotNull(søknadPersistance.søknadprosess)
    }

    @Test
    fun `Start ny søknadprosess, trigget av innsending_ferdigstilt fra dp-mottak`() {
        fakeUnleash.enable(FEATURE_MOTTA_SØKNAD)
        testRapid.sendTestMessage(innsendingFerdigstiltJson)
        assertEquals(1, testRapid.inspektør.size)
        assertNotNull(søknadPersistance.søknadprosess)
    }

    @Test
    fun `Ikke start ny søknadprosess, når motta-søknad-flagg er av`() {
        fakeUnleash.disable(FEATURE_MOTTA_SØKNAD)
        testRapid.sendTestMessage(innsendingFerdigstiltJson)
        assertEquals(0, testRapid.inspektør.size)
    }

    private fun nySøknadMelding() =
        mutableMapOf<String, Any>(
            "@id" to UUID.randomUUID(),
            "@event_name" to "Søknad",
            "@opprettet" to LocalDateTime.now(),
            "fnr" to "fødelsnummer",
            "aktørId" to "aktør",
            "søknadsId" to "mf68etellerannet"
        ).let { JsonMessage.newMessage(it).toJson() }

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
