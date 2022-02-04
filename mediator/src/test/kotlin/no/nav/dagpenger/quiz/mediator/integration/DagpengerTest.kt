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
import no.nav.dagpenger.quiz.mediator.helpers.juli
import no.nav.dagpenger.quiz.mediator.helpers.juni
import no.nav.dagpenger.quiz.mediator.helpers.mai
import no.nav.dagpenger.quiz.mediator.meldinger.DagpengerService
import no.nav.dagpenger.quiz.mediator.meldinger.FaktumSvarService
import no.nav.dagpenger.quiz.mediator.meldinger.asPeriode
import no.nav.dagpenger.quiz.mediator.meldinger.asTekst
import no.nav.dagpenger.quiz.mediator.soknad.Dagpenger
import no.nav.dagpenger.quiz.mediator.soknad.dagpenger.DummySeksjon
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

            besvar(DummySeksjon.`dummy boolean`, true)
            testRapid.inspektør.message(2).let {
                assertEquals("NySøknad", it["@event_name"].asText())
                assertEquals(true, it.hentSvar(DummySeksjon.`dummy boolean`).asBoolean())
            }

            besvar(DummySeksjon.`dummy valg`, Envalg("faktum.dummy-valg.svar.ja"))
            testRapid.inspektør.message(3).let {
                assertEquals("NySøknad", it["@event_name"].asText())
                assertEquals("faktum.dummy-valg.svar.ja", it.hentSvar(DummySeksjon.`dummy valg`).asText())
            }

            besvar(DummySeksjon.`dummy subfaktum tekst`, Tekst("subfaktumsvar"))
            testRapid.inspektør.message(4).let {
                assertEquals("NySøknad", it["@event_name"].asText())

                assertEquals("subfaktumsvar", it.hentSvar(DummySeksjon.`dummy subfaktum tekst`).asText())
            }

            besvar(DummySeksjon.`dummy flervalg`, Flervalg("faktum.dummy-flervalg.svar.1", "faktum.dummy-flervalg.svar.2"))
            testRapid.inspektør.message(5).let {
                assertEquals("NySøknad", it["@event_name"].asText())

                val svar = it.hentSvar(DummySeksjon.`dummy flervalg`)
                svar is ArrayNode
                assertEquals(svar[0].asText(), "faktum.dummy-flervalg.svar.1")
                assertEquals(svar[1].asText(), "faktum.dummy-flervalg.svar.2")
            }

            besvar(DummySeksjon.`dummy dropdown`, Envalg("faktum.dummy-dropdown.svar.1"))
            testRapid.inspektør.message(6).let {
                assertEquals("NySøknad", it["@event_name"].asText())
                assertEquals("faktum.dummy-dropdown.svar.1", it.hentSvar(DummySeksjon.`dummy dropdown`).asText())
            }

            besvar(DummySeksjon.`dummy int`, 1)
            testRapid.inspektør.message(7).let {
                assertEquals("NySøknad", it["@event_name"].asText())

                assertEquals(1, it.hentSvar(DummySeksjon.`dummy int`).asInt())
            }

            besvar(DummySeksjon.`dummy double`, 1.5)
            testRapid.inspektør.message(8).let {
                assertEquals("NySøknad", it["@event_name"].asText())

                assertEquals(1.5, it.hentSvar(DummySeksjon.`dummy double`).asDouble())
            }

            besvar(DummySeksjon.`dummy tekst`, Tekst("tekstsvar"))
            testRapid.inspektør.message(9).let {
                assertEquals("NySøknad", it["@event_name"].asText())

                assertEquals("tekstsvar", it.hentSvar(DummySeksjon.`dummy tekst`).asText())
            }

            besvar(DummySeksjon.`dummy localdate`, 1.juli())
            testRapid.inspektør.message(10).let {
                assertEquals("NySøknad", it["@event_name"].asText())

                assertEquals(1.juli(), it.hentSvar(DummySeksjon.`dummy localdate`).asLocalDate())
            }

            besvar(DummySeksjon.`dummy periode`, Periode(1.januar(), 1.februar()))
            testRapid.inspektør.message(11).let {
                assertEquals("NySøknad", it["@event_name"].asText())

                val svarene = it.hentSvar(DummySeksjon.`dummy periode`)
                assertEquals(1.januar(), svarene["fom"].asLocalDate())
                assertEquals(1.februar(), svarene["tom"].asOptionalLocalDate())
            }

            besvar(DummySeksjon.`generator dummy subfaktum tekst`, Tekst("subfaktumDefinertIGeneratorsvar"))
            testRapid.inspektør.message(12).let {
                assertEquals("NySøknad", it["@event_name"].asText())

                assertEquals("subfaktumDefinertIGeneratorsvar", it.hentSvar(DummySeksjon.`generator dummy subfaktum tekst`).asText())
            }

            besvar(
                DummySeksjon.`dummy generator`,
                listOf(
                    listOf(
                        "${DummySeksjon.`generator dummy boolean`}" to true,
                        "${DummySeksjon.`generator dummy valg`}" to Envalg("faktum.generator-dummy-valg.svar.ja"),
                        "${DummySeksjon.`generator dummy flervalg`}" to Flervalg("faktum.generator-dummy-flervalg.svar.1", "faktum.generator-dummy-flervalg.svar.2"),
                        "${DummySeksjon.`generator dummy dropdown`}" to Envalg("faktum.generator-dummy-dropdown.svar.1"),
                        "${DummySeksjon.`generator dummy int`}" to 4,
                        "${DummySeksjon.`generator dummy double`}" to 2.5,
                        "${DummySeksjon.`generator dummy tekst`}" to Tekst("svartekst"),
                        "${DummySeksjon.`generator dummy localdate`}" to 1.april(),
                        "${DummySeksjon.`generator dummy periode`}" to Periode(1.mai(), 1.juni()),
                    )
                )
            )

            testRapid.inspektør.message(13).let {
                assertEquals("NySøknad", it["@event_name"].asText())

                val svarliste = it.hentSvar(DummySeksjon.`dummy generator`)
                val førsteSvarelement = svarliste[0]

                assertEquals(true, førsteSvarelement["faktum.generator-dummy-boolean"].asBoolean())
                assertEquals("faktum.generator-dummy-valg.svar.ja", førsteSvarelement["faktum.generator-dummy-valg"].asText())

                val flervalgSvar = førsteSvarelement["faktum.generator-dummy-flervalg"]
                assertEquals("faktum.generator-dummy-flervalg.svar.1", flervalgSvar[0].asText())
                assertEquals("faktum.generator-dummy-flervalg.svar.2", flervalgSvar[1].asText())

                assertEquals("faktum.generator-dummy-dropdown.svar.1", førsteSvarelement["faktum.generator-dummy-dropdown"].asText())
                assertEquals(4, førsteSvarelement["faktum.generator-dummy-int"].asInt())
                assertEquals(2.5, førsteSvarelement["faktum.generator-dummy-double"].asDouble())
                assertEquals(Tekst("svartekst"), førsteSvarelement["faktum.generator-dummy-tekst"].asTekst())
                assertEquals(1.april(), førsteSvarelement["faktum.generator-dummy-localdate"].asLocalDate())
                assertEquals(Periode(1.mai(), 1.juni()), førsteSvarelement["faktum.generator-dummy-periode"].asPeriode())
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
