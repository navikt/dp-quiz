package no.nav.dagpenger.quiz.mediator.integration

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import no.nav.dagpenger.model.faktum.Envalg
import no.nav.dagpenger.model.faktum.Flervalg
import no.nav.dagpenger.model.faktum.Periode
import no.nav.dagpenger.model.faktum.Tekst
import no.nav.dagpenger.quiz.mediator.db.FaktumTable
import no.nav.dagpenger.quiz.mediator.db.ResultatRecord
import no.nav.dagpenger.quiz.mediator.db.SøknadRecord
import no.nav.dagpenger.quiz.mediator.helpers.Postgres
import no.nav.dagpenger.quiz.mediator.helpers.april
import no.nav.dagpenger.quiz.mediator.helpers.februar
import no.nav.dagpenger.quiz.mediator.helpers.januar
import no.nav.dagpenger.quiz.mediator.helpers.juni
import no.nav.dagpenger.quiz.mediator.helpers.mai
import no.nav.dagpenger.quiz.mediator.meldinger.DagpengerService
import no.nav.dagpenger.quiz.mediator.meldinger.FaktumSvarService
import no.nav.dagpenger.quiz.mediator.meldinger.asTekst
import no.nav.dagpenger.quiz.mediator.soknad.Dagpenger
import no.nav.helse.rapids_rivers.asLocalDate
import no.nav.helse.rapids_rivers.asOptionalLocalDate
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
                    emptyList<String>(),
                    it["@behov"].map { it.asText() }
                )
            }

            testRapid.inspektør.message(1).let {
                assertFalse { it.toString().contains(""""svar":""") }
            }

            besvar(Dagpenger.`dummy-boolean`, true)
            testRapid.inspektør.message(2).let {
                assertEquals("NySøknad", it["@event_name"].asText())
                assertEquals(true, it.hentSvar(Dagpenger.`dummy-boolean`).asBoolean())
            }

            besvar(Dagpenger.`dummy-envalg`, Envalg("faktum.dummy-valg.svar.ja"))
            testRapid.inspektør.message(3).let {
                assertEquals("NySøknad", it["@event_name"].asText())
                assertEquals("faktum.dummy-valg.svar.ja", it.hentSvar(Dagpenger.`dummy-envalg`).asText())
            }

            besvar(Dagpenger.`dummy-flervalg`, Flervalg("faktum.dummy-flervalg.svar.1", "faktum.dummy-flervalg.svar.2"))
            testRapid.inspektør.message(4).let {
                assertEquals("NySøknad", it["@event_name"].asText())

                val svar = it.hentSvar(Dagpenger.`dummy-flervalg`)
                svar is ArrayNode
                assertEquals(svar[0].asText(), "faktum.dummy-flervalg.svar.1")
                assertEquals(svar[1].asText(), "faktum.dummy-flervalg.svar.2")
            }

            besvar(Dagpenger.`dummy-heltall`, 1)
            testRapid.inspektør.message(5).let {
                assertEquals("NySøknad", it["@event_name"].asText())

                assertEquals(1, it.hentSvar(Dagpenger.`dummy-heltall`).asInt())
            }

            besvar(Dagpenger.`dummy-desimaltall`, 1.5)
            testRapid.inspektør.message(6).let {
                assertEquals("NySøknad", it["@event_name"].asText())

                assertEquals(1.5, it.hentSvar(Dagpenger.`dummy-desimaltall`).asDouble())
            }

            besvar(Dagpenger.`dummy-tekst`, Tekst("tekstsvar"))
            testRapid.inspektør.message(7).let {
                assertEquals("NySøknad", it["@event_name"].asText())

                assertEquals("tekstsvar", it.hentSvar(Dagpenger.`dummy-tekst`).asText())
            }

            besvar(Dagpenger.`dummy-periode`, Periode(1.januar(), 1.februar()))
            testRapid.inspektør.message(8).let {
                assertEquals("NySøknad", it["@event_name"].asText())

                val svarene = it.hentSvar(Dagpenger.`dummy-periode`)
                assertEquals(1.januar(), svarene["fom"].asLocalDate())
                assertEquals(1.februar(), svarene["tom"].asOptionalLocalDate())
            }

            besvar(
                Dagpenger.`dummy-generator`,
                listOf(
                    listOf(
                        "${Dagpenger.`generator dummy-boolean`}" to true,
                        "${Dagpenger.`generator dummy-envalg`}" to Envalg("faktum.generator-dummy-valg.svar.ja"),
                        "${Dagpenger.`generator dummy-flervalg`}" to Flervalg("faktum.generator-dummy-flervalg.svar.1", "faktum.generator-dummy-flervalg.svar.2"),
                        "${Dagpenger.`generator dummy-heltall`}" to 4,
                        "${Dagpenger.`generator dummy-desimaltall`}" to 2.5,
                        "${Dagpenger.`generator dummy-tekst`}" to Tekst("svartekst"),
                        "${Dagpenger.`generator dummy-dato`}" to 1.april(),
                        "${Dagpenger.`generator dummy-periode`}" to Periode(1.mai(), 1.juni()),
                    )
                )
            )

            testRapid.inspektør.message(9).let {
                assertEquals("NySøknad", it["@event_name"].asText())

                val svarliste = it.hentSvar(Dagpenger.`dummy-generator`)
                val førsteSvarelement = svarliste[0]
                assertEquals(true, førsteSvarelement["faktum.generator-dummy-boolean"].asBoolean())
                assertEquals(4, førsteSvarelement["faktum.generator-dummy-int"].asInt())
                assertEquals(2.5, førsteSvarelement["faktum.generator-dummy-desimaltall"].asDouble())
                assertEquals(1.april(), førsteSvarelement["faktum.generator-dummy-localdate"].asLocalDate())
                assertEquals(Tekst("svartekst"), førsteSvarelement["faktum.generator-dummy-tekst"].asTekst())
                assertEquals("faktum.generator-dummy-valg.svar.ja", førsteSvarelement["faktum.generator-dummy-valg"].asText())

                val flervalgSvar = førsteSvarelement["faktum.generator-dummy-flervalg"]
                assertEquals("faktum.generator-dummy-flervalg.svar.1", flervalgSvar[0].asText())
                assertEquals("faktum.generator-dummy-flervalg.svar.2", flervalgSvar[1].asText())
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
