package no.nav.dagpenger.quiz.mediator.integration

import no.nav.dagpenger.quiz.mediator.db.FaktumTable
import no.nav.dagpenger.quiz.mediator.db.ResultatRecord
import no.nav.dagpenger.quiz.mediator.db.SøknadRecord
import no.nav.dagpenger.quiz.mediator.helpers.Postgres
import no.nav.dagpenger.quiz.mediator.meldinger.FaktumSvarService
import no.nav.dagpenger.quiz.mediator.meldinger.NySøknadBehovLøser
import no.nav.dagpenger.quiz.mediator.soknad.dagpenger.Dagpenger
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.UUID

internal class NySøknadBehovLøserTest : SøknadBesvarer() {

    @BeforeEach
    fun setup() {
        Postgres.withMigratedDb {
            Dagpenger.registrer { søknad -> FaktumTable(søknad) }
            val søknadPersistence = SøknadRecord()
            val resultatPersistence = ResultatRecord()
            testRapid = TestRapid().also {
                FaktumSvarService(
                    søknadPersistence = søknadPersistence,
                    resultatPersistence = resultatPersistence,
                    rapidsConnection = it
                )
                NySøknadBehovLøser(søknadPersistence, it)
            }
        }
    }

    @AfterEach
    fun reset() {
        testRapid.reset()
    }

    @Test
    fun `Hent alle fakta happy path`() {

        withSøknad(nySøknadBehov) { _ ->
            assertEquals(2, testRapid.inspektør.size)
            melding(0).let {
                assertEquals("søker_oppgave", it["@event_name"].asText())
                assertFalse { it.toString().contains(""""svar":""") }
            }

            melding(1).let {
                assertEquals("behov", it["@event_name"].asText())
                assertEquals(
                    listOf("NySøknad"),
                    it["@behov"].map { it.asText() }
                )
                assertFalse(it["@løsning"]["NySøknad"].isNull, "NySøknad behov skal besvares med søknad id")
            }
        }
    }

    @Test
    fun `Ignore nysøknad med fakta`() {
        testRapid.sendTestMessage(ferdigNySøknadløsning)
        assertEquals(0, testRapid.inspektør.size)
    }

    private val søknadUUID = UUID.randomUUID()

    //language=JSON
    private val ferdigNySøknadløsning =
        """
        {
          "@event_name": "behov",
          "@behov" : ["NySøknad"],
          "@opprettet": "${LocalDateTime.now()}",
          "@id": "${UUID.randomUUID()}",
          "søknad_uuid": "$søknadUUID",
          "ident": "123456789",
          "@løsning": {
              "NySøknad" : "$søknadUUID"
          }
        }
        
        """.trimIndent()

    //language=JSON
    private val nySøknadBehov =
        """
        {
          "@event_name": "behov",
          "@behov" : ["NySøknad"],
          "@opprettet": "${LocalDateTime.now()}",
          "@id": "${UUID.randomUUID()}",
          "søknad_uuid": "$søknadUUID",
          "ident": "123456789"
        }
        
        """.trimIndent()
}
