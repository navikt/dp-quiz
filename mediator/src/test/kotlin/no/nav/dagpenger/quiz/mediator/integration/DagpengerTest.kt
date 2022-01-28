package no.nav.dagpenger.quiz.mediator.integration

import com.fasterxml.jackson.databind.JsonNode
import no.nav.dagpenger.model.faktum.Periode
import no.nav.dagpenger.model.faktum.Tekst
import no.nav.dagpenger.quiz.mediator.db.FaktumTable
import no.nav.dagpenger.quiz.mediator.db.ResultatRecord
import no.nav.dagpenger.quiz.mediator.db.SøknadRecord
import no.nav.dagpenger.quiz.mediator.helpers.Postgres
import no.nav.dagpenger.quiz.mediator.meldinger.DagpengerService
import no.nav.dagpenger.quiz.mediator.meldinger.FaktumSvarService
import no.nav.dagpenger.quiz.mediator.soknad.Dagpenger
import no.nav.helse.rapids_rivers.asLocalDate
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
    private val tomorrow = today.plusDays(1)

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
                    listOf("PersonaliaAlder", "PersonaliaNavn", "enEllerAnnenPeriod", "PersonEpost", "PersonTelefonnummer", "Arbeidsforhold"),
                    it["@behov"].map { it.asText() }
                )
            }

            testRapid.inspektør.message(1).let {
                assertEquals("NySøknad", it["@event_name"].asText())
                assertNotNull(it["fakta"])
                assertEquals(0, it["fakta"].filter { it["svar"] != null }.size)
            }

            besvar(
                Dagpenger.`personalia alder`,
                20
            )
            testRapid.inspektør.message(2).let {
                assertEquals("NySøknad", it["@event_name"].asText())
                assertEquals(20, it.hentSvar(Dagpenger.`personalia alder`).asInt())
            }

            besvar(
                Dagpenger.arbeidsforhold,
                listOf(
                    listOf(
                        "${Dagpenger.`arbeidsforhold fra og med`}.1" to today,
                        "${Dagpenger.`arbeidsforhold til og med`}.1" to tomorrow,
                    )
                )
            )
            testRapid.inspektør.message(3).let {
                assertEquals("NySøknad", it["@event_name"].asText())
                val svarList = it.hentSvar(Dagpenger.arbeidsforhold).get(0).toList()
                assertEquals(svarList[0].asLocalDate(), today)
                assertEquals(svarList[1].asLocalDate(), tomorrow)
            }

            besvar(
                Dagpenger.`personalia navn`,
                Tekst("et navn")
            )
            testRapid.inspektør.message(4).let {
                assertEquals("NySøknad", it["@event_name"].asText())
                assertEquals("et navn", it.hentSvar(Dagpenger.`personalia navn`).asText())
            }

            besvar(
                Dagpenger.`en eller annen period`,
                Periode(today, tomorrow)
            )
            testRapid.inspektør.message(5).let {
                assertEquals("NySøknad", it["@event_name"].asText())
                val svar = it.hentSvar(Dagpenger.`en eller annen period`)
                assertEquals(svar["fom"].asLocalDate(), today)
                assertEquals(svar["tom"].asLocalDate(), tomorrow)
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
