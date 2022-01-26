package no.nav.dagpenger.quiz.mediator.integration

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.dagpenger.model.faktum.Envalg
import no.nav.dagpenger.quiz.mediator.db.FaktumTable
import no.nav.dagpenger.quiz.mediator.db.ResultatRecord
import no.nav.dagpenger.quiz.mediator.db.SøknadRecord
import no.nav.dagpenger.quiz.mediator.helpers.Postgres
import no.nav.dagpenger.quiz.mediator.meldinger.DagpengerService
import no.nav.dagpenger.quiz.mediator.meldinger.FaktumSvarService
import no.nav.dagpenger.quiz.mediator.soknad.Dagpenger
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

internal class DagpengerTest : SøknadBesvarer() {

    private val today = LocalDate.now()

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

    val objectMapper = jacksonObjectMapper().configure(SerializationFeature.INDENT_OUTPUT, true)

    @Test
    fun `Hent alle fakta happy path`() {

        withSøknad(nySøknad) { besvar ->
            testRapid.inspektør.message(0).let {
                assertEquals("faktum_svar", it["@event_name"].asText())
                assertEquals(
                    emptyList(),
                    it["@behov"].map { it.asText() }
                )
            }

            testRapid.inspektør.message(1).let {
                val prettyPrint = objectMapper.writeValueAsString(it)
                println("### Alle fakta, uten svar \n$prettyPrint")
            }

            besvar(
                Dagpenger.`for dummy-boolean`,
                true
            )
            testRapid.inspektør.message(2).let {
                assertEquals("NySøknad", it["@event_name"].asText())
                assertEquals(true, it.hentSvar(Dagpenger.`for dummy-boolean`).asBoolean())
            }

            besvar(
                Dagpenger.`for dummy-envalg`,
                Envalg("faktum.dummy-valg.svar.ja")
            )
            testRapid.inspektør.message(3).let {
                val prettyPrint = objectMapper.writeValueAsString(it)
                println("### Index 3 \n$prettyPrint")
                assertEquals("NySøknad", it["@event_name"].asText())
                assertEquals("faktum.dummy-valg.svar.ja", it.hentSvar(Dagpenger.`for dummy-envalg`).asText())
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
