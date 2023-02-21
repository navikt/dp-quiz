package no.nav.dagpenger.quiz.mediator.integration

import no.nav.dagpenger.quiz.mediator.db.FaktumTable
import no.nav.dagpenger.quiz.mediator.db.ProsessRepositoryImpl
import no.nav.dagpenger.quiz.mediator.db.ResultatRecord
import no.nav.dagpenger.quiz.mediator.helpers.Postgres
import no.nav.dagpenger.quiz.mediator.meldinger.FaktumSvarService
import no.nav.dagpenger.quiz.mediator.meldinger.NyProsessBehovLøser
import no.nav.dagpenger.quiz.mediator.soknad.dagpenger.Dagpenger
import no.nav.dagpenger.quiz.mediator.soknad.innsending.Innsending
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import java.time.LocalDateTime
import java.util.UUID

internal class NyProsessfaktaBehovLøserTest : SøknadBesvarer() {
    @BeforeEach
    fun setup() {
        Postgres.withMigratedDb {
            Dagpenger.registrer(::FaktumTable)
            Innsending.registrer(::FaktumTable)
            val søknadPersistence = ProsessRepositoryImpl()
            val resultatPersistence = ResultatRecord()
            testRapid = TestRapid().also {
                FaktumSvarService(
                    prosessRepository = søknadPersistence,
                    resultatPersistence = resultatPersistence,
                    rapidsConnection = it,
                )
                NyProsessBehovLøser(søknadPersistence, it)
            }
        }
    }

    @AfterEach
    fun reset() {
        testRapid.reset()
    }

    @Test
    fun `Hent alle fakta happy path`() {
        withSøknad(nySøknadBehov("Dagpenger")) { _ ->
            assertEquals(2, testRapid.inspektør.size)

            melding(0).let {
                assertEquals("behov", it["@event_name"].asText())
                assertEquals(
                    listOf("NySøknad"),
                    it["@behov"].map { it.asText() },
                )
                assertFalse(it["@løsning"]["NySøknad"].isNull, "NySøknad behov skal besvares med søknad id")
            }

            melding(1).let {
                assertEquals("søker_oppgave", it["@event_name"].asText())
                assertFalse { it.toString().contains(""""svar":""") }
            }
        }
    }

    @Test
    fun `Oppretter ny prosess for innsending`() {
        val uuid = triggNySøknadsprosess(nySøknadBehov("Innsending"))
        assertEquals(2, testRapid.inspektør.size)
        assertDoesNotThrow {
            UUID.fromString(uuid)
        }

        assertEquals("faktum.generell-innsending.hvorfor", testRapid.inspektør.message(1)["seksjoner"][0]["fakta"][0]["beskrivendeId"].asText())
    }

    @Test
    fun `Ignore nysøknad med fakta`() {
        testRapid.sendTestMessage(ferdigNySøknadløsning)
        assertEquals(0, testRapid.inspektør.size)
    }

    private val søknadUUID = UUID.randomUUID()
    private val ferdigNySøknadløsning =
        //language=JSON
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

    private fun nySøknadBehov(prosessnavn: String) =
        //language=JSON
        """
        {
          "@event_name": "behov",
          "@behov" : ["NySøknad"],
          "@opprettet": "${LocalDateTime.now()}",
          "@id": "${UUID.randomUUID()}",
          "søknad_uuid": "$søknadUUID",
          "ident": "123456789",
          "prosessnavn": "$prosessnavn"
        }
        
        """.trimIndent()
}
