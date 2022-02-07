package no.nav.dagpenger.quiz.mediator.integration

import com.fasterxml.jackson.databind.JsonNode
import no.nav.dagpenger.quiz.mediator.db.FaktumTable
import no.nav.dagpenger.quiz.mediator.db.ResultatRecord
import no.nav.dagpenger.quiz.mediator.db.SøknadRecord
import no.nav.dagpenger.quiz.mediator.helpers.Postgres
import no.nav.dagpenger.quiz.mediator.meldinger.DagpengerService
import no.nav.dagpenger.quiz.mediator.meldinger.FaktumSvarService
import no.nav.dagpenger.quiz.mediator.soknad.Dagpenger
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.UUID

internal class DagpengerTest : SøknadBesvarer() {

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
                DagpengerService(søknadPersistence, it)
            }
        }
    }

    @AfterEach
    fun reset() {
        testRapid.reset()
    }

    private fun JsonNode.hentSvar(id: Int): JsonNode {
        return this["fakta"].let { faktaNode ->
            assertNotNull(faktaNode)
            faktaNode.single { node ->
                node["id"].asInt() == id
            }["svar"]
        }
    }

    @Test
    fun `Hent alle fakta happy path`() {

        withSøknad(nySøknad) { besvar ->
            testRapid.inspektør.message(0).let {
                assertEquals("faktum_svar", it["@event_name"].asText())
                assertEquals(
                    listOf("Barn"),
                    it["@behov"].map { it.asText() }
                )
            }

            testRapid.inspektør.message(1).let {
                assertFalse { it.toString().contains(""""svar":""") }
            }
        }
    }

    @Test
    fun `Ignore nysøknad med fakta`() {
        testRapid.sendTestMessage(ferdigSøknad)
        assertEquals(0, testRapid.inspektør.size)
    }

    private val søknadUUID = UUID.randomUUID()

    //language=JSON
    private val ferdigSøknad =
        """
        {
          "@event_name": "NySøknad",
          "@opprettet": "${LocalDateTime.now()}",
          "@id": "${UUID.randomUUID()}",
          "søknad_uuid": "${UUID.randomUUID()}",
          "fødselsnummer": "123456789",
          "fakta": []
        }
        
        """.trimIndent()

    //language=JSON
    private val nySøknad =
        """
        {
          "@event_name": "NySøknad",
          "@opprettet": "${LocalDateTime.now()}",
          "@id": "${UUID.randomUUID()}",
          "søknad_uuid": "$søknadUUID",
          "fødselsnummer": "123456789"
        }
        
        """.trimIndent()
}
