package no.nav.dagpenger.quiz.mediator.integration.dummy

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import no.nav.dagpenger.model.faktum.Envalg
import no.nav.dagpenger.model.faktum.Flervalg
import no.nav.dagpenger.model.faktum.Land
import no.nav.dagpenger.model.faktum.Periode
import no.nav.dagpenger.model.faktum.Tekst
import no.nav.dagpenger.quiz.mediator.db.FaktumTable
import no.nav.dagpenger.quiz.mediator.db.ResultatRecord
import no.nav.dagpenger.quiz.mediator.db.SøknadRecord
import no.nav.dagpenger.quiz.mediator.helpers.Postgres
import no.nav.dagpenger.quiz.mediator.helpers.SøknadSeksjonsTester
import no.nav.dagpenger.quiz.mediator.helpers.april
import no.nav.dagpenger.quiz.mediator.helpers.februar
import no.nav.dagpenger.quiz.mediator.helpers.januar
import no.nav.dagpenger.quiz.mediator.helpers.juli
import no.nav.dagpenger.quiz.mediator.helpers.juni
import no.nav.dagpenger.quiz.mediator.helpers.mai
import no.nav.dagpenger.quiz.mediator.integration.SøknadBesvarer
import no.nav.dagpenger.quiz.mediator.meldinger.DagpengerService
import no.nav.dagpenger.quiz.mediator.meldinger.FaktumSvarService
import no.nav.dagpenger.quiz.mediator.meldinger.asPeriode
import no.nav.dagpenger.quiz.mediator.meldinger.asTekst
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

internal class DummySeksjonTest : SøknadBesvarer() {

    @BeforeEach
    fun setup() {

        Postgres.withMigratedDb {
            SøknadSeksjonsTester.registrer { søknad -> FaktumTable(søknad) }
            val søknadPersistence = SøknadRecord()
            val resultatPersistence = ResultatRecord()
            testRapid = TestRapid().also {
                FaktumSvarService(
                    søknadPersistence = søknadPersistence,
                    resultatPersistence = resultatPersistence,
                    rapidsConnection = it
                )
                DagpengerService(søknadPersistence, it, SøknadSeksjonsTester.VERSJON_ID)
            }
        }
    }

    private fun JsonNode.hentSvar(id: Int): JsonNode {
        return this["fakta"].let { faktaNode ->
            assertNotNull(faktaNode)
            faktaNode.hentSvarMedId(id.toString())
        }
    }

    private fun JsonNode.hentGeneratorSvar(id: String): JsonNode {
        return this.let { faktaNode ->
            assertNotNull(faktaNode)
            faktaNode.hentSvarMedId(id)
        }
    }

    private fun JsonNode.hentSvarMedId(
        id: String
    ) = this.single { node ->
        node["id"].asText() == id
    }["svar"]

    @AfterEach
    fun reset() {
        testRapid.reset()
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

            besvar(
                DummySeksjon.`dummy flervalg`,
                Flervalg("faktum.dummy-flervalg.svar.1", "faktum.dummy-flervalg.svar.2")
            )
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

                assertEquals(
                    "subfaktumDefinertIGeneratorsvar",
                    it.hentSvar(DummySeksjon.`generator dummy subfaktum tekst`).asText()
                )
            }

            besvar(
                DummySeksjon.`dummy generator`,
                generatorsvar(
                    "${DummySeksjon.`generator dummy boolean`}" to true,
                    "${DummySeksjon.`generator dummy valg`}" to Envalg("faktum.generator-dummy-valg.svar.ja"),
                    "${DummySeksjon.`generator dummy flervalg`}" to Flervalg(
                        "faktum.generator-dummy-flervalg.svar.1",
                        "faktum.generator-dummy-flervalg.svar.2"
                    ),
                    "${DummySeksjon.`generator dummy dropdown`}" to Envalg("faktum.generator-dummy-dropdown.svar.1"),
                    "${DummySeksjon.`generator dummy int`}" to 4,
                    "${DummySeksjon.`generator dummy double`}" to 2.5,
                    "${DummySeksjon.`generator dummy tekst`}" to Tekst("svartekst"),
                    "${DummySeksjon.`generator dummy localdate`}" to 1.april(),
                    "${DummySeksjon.`generator dummy periode`}" to Periode(1.mai(), 1.juni()),
                )
            )

            testRapid.inspektør.message(13).let {
                assertEquals("NySøknad", it["@event_name"].asText())

                val svarliste = it.hentSvar(DummySeksjon.`dummy generator`)
                val førsteSvarelement = svarliste[0]

                assertEquals(
                    true,
                    førsteSvarelement.hentGeneratorSvar("${DummySeksjon.`generator dummy boolean`}.1").asBoolean()
                )
                assertEquals(
                    "faktum.generator-dummy-valg.svar.ja",
                    førsteSvarelement.hentGeneratorSvar("${DummySeksjon.`generator dummy valg`}.1").asText()
                )

                val flervalgSvar = førsteSvarelement.hentGeneratorSvar("${DummySeksjon.`generator dummy flervalg`}.1")
                assertEquals("faktum.generator-dummy-flervalg.svar.1", flervalgSvar[0].asText())
                assertEquals("faktum.generator-dummy-flervalg.svar.2", flervalgSvar[1].asText())

                assertEquals(
                    "faktum.generator-dummy-dropdown.svar.1",
                    førsteSvarelement.hentGeneratorSvar("${DummySeksjon.`generator dummy dropdown`}.1").asText()
                )
                assertEquals(4, førsteSvarelement.hentGeneratorSvar("${DummySeksjon.`generator dummy int`}.1").asInt())
                assertEquals(
                    2.5,
                    førsteSvarelement.hentGeneratorSvar("${DummySeksjon.`generator dummy double`}.1").asDouble()
                )
                assertEquals(
                    Tekst("svartekst"),
                    førsteSvarelement.hentGeneratorSvar("${DummySeksjon.`generator dummy tekst`}.1").asTekst()
                )
                assertEquals(
                    1.april(),
                    førsteSvarelement.hentGeneratorSvar("${DummySeksjon.`generator dummy localdate`}.1").asLocalDate()
                )
                assertEquals(
                    Periode(1.mai(), 1.juni()),
                    førsteSvarelement.hentGeneratorSvar("${DummySeksjon.`generator dummy periode`}.1").asPeriode()
                )
            }

            besvar(DummySeksjon.`dummy land`, Land("NOR"))
            testRapid.inspektør.message(14).let {
                assertEquals("NySøknad", it["@event_name"].asText())
                assertEquals("NOR", it.hentSvar(DummySeksjon.`dummy land`).asText())
            }
        }
    }

    @Test
    fun `Skal kunne svare på et subset av fakta i et generatorfaktum, og senere svare på flere subset, uten å miste data`() {
        withSøknad(nySøknad) { besvar ->
            besvar(
                DummySeksjon.`dummy generator`,
                generatorsvar(
                    "${DummySeksjon.`generator dummy boolean`}" to true,
                    "${DummySeksjon.`generator dummy valg`}" to Envalg("faktum.generator-dummy-valg.svar.ja"),
                    "${DummySeksjon.`generator dummy flervalg`}" to Flervalg(
                        "faktum.generator-dummy-flervalg.svar.1",
                        "faktum.generator-dummy-flervalg.svar.2"
                    ),
                )
            )

            testRapid.inspektør.message(2).let {
                assertEquals("NySøknad", it["@event_name"].asText())

                val svarliste = it.hentSvar(DummySeksjon.`dummy generator`)
                val førsteSvarelement = svarliste[0]
                assertEquals(
                    true,
                    førsteSvarelement.hentGeneratorSvar("${DummySeksjon.`generator dummy boolean`}.1").asBoolean()
                )
                assertEquals(
                    "faktum.generator-dummy-valg.svar.ja",
                    førsteSvarelement.hentGeneratorSvar("${DummySeksjon.`generator dummy valg`}.1").asText()
                )
                assertEquals(
                    listOf("faktum.generator-dummy-flervalg.svar.1", "faktum.generator-dummy-flervalg.svar.2"),
                    førsteSvarelement.hentGeneratorSvar("${DummySeksjon.`generator dummy flervalg`}.1")
                        .map { verdi -> verdi.asText() }
                )
            }

            besvar(
                DummySeksjon.`dummy generator`,
                generatorsvar(
                    "${DummySeksjon.`generator dummy dropdown`}" to Envalg("faktum.generator-dummy-dropdown.svar.1"),
                    "${DummySeksjon.`generator dummy int`}" to 4,
                    "${DummySeksjon.`generator dummy double`}" to 2.5,
                )
            )

            testRapid.inspektør.message(3).let {
                assertEquals("NySøknad", it["@event_name"].asText())

                val svarliste = it.hentSvar(DummySeksjon.`dummy generator`)
                val førsteSvarelement = svarliste[0]

                assertEquals(
                    true,
                    førsteSvarelement.hentGeneratorSvar("${DummySeksjon.`generator dummy boolean`}.1").asBoolean()
                )

                assertEquals(
                    "faktum.generator-dummy-valg.svar.ja",
                    førsteSvarelement.hentGeneratorSvar("${DummySeksjon.`generator dummy valg`}.1").asText()
                )
                assertEquals(
                    listOf("faktum.generator-dummy-flervalg.svar.1", "faktum.generator-dummy-flervalg.svar.2"),
                    førsteSvarelement.hentGeneratorSvar("${DummySeksjon.`generator dummy flervalg`}.1")
                        .map { verdi -> verdi.asText() }
                )

                assertEquals(
                    "faktum.generator-dummy-dropdown.svar.1",
                    førsteSvarelement.hentGeneratorSvar("${DummySeksjon.`generator dummy dropdown`}.1").asText()
                )

                assertEquals(
                    4,
                    førsteSvarelement.hentGeneratorSvar("${DummySeksjon.`generator dummy int`}.1").asInt()
                )

                assertEquals(
                    2.5,
                    førsteSvarelement.hentGeneratorSvar("${DummySeksjon.`generator dummy double`}.1").asDouble()
                )
            }

            besvar(
                DummySeksjon.`dummy generator`,
                generatorsvar(
                    "${DummySeksjon.`generator dummy tekst`}" to Tekst("svartekst"),
                    "${DummySeksjon.`generator dummy localdate`}" to 1.april(),
                    "${DummySeksjon.`generator dummy periode`}" to Periode(1.mai(), 1.juni()),
                )
            )

            testRapid.inspektør.message(4).let {
                assertEquals("NySøknad", it["@event_name"].asText())

                val svarliste = it.hentSvar(DummySeksjon.`dummy generator`)
                val førsteSvarelement = svarliste[0]

                assertEquals(
                    true,
                    førsteSvarelement.hentGeneratorSvar("${DummySeksjon.`generator dummy boolean`}.1").asBoolean()
                )

                assertEquals(
                    "faktum.generator-dummy-valg.svar.ja",
                    førsteSvarelement.hentGeneratorSvar("${DummySeksjon.`generator dummy valg`}.1").asText()
                )
                assertEquals(
                    listOf("faktum.generator-dummy-flervalg.svar.1", "faktum.generator-dummy-flervalg.svar.2"),
                    førsteSvarelement.hentGeneratorSvar("${DummySeksjon.`generator dummy flervalg`}.1")
                        .map { verdi -> verdi.asText() }
                )

                assertEquals(
                    "faktum.generator-dummy-dropdown.svar.1",
                    førsteSvarelement.hentGeneratorSvar("${DummySeksjon.`generator dummy dropdown`}.1").asText()
                )

                assertEquals(
                    4,
                    førsteSvarelement.hentGeneratorSvar("${DummySeksjon.`generator dummy int`}.1").asInt()
                )

                assertEquals(
                    2.5,
                    førsteSvarelement.hentGeneratorSvar("${DummySeksjon.`generator dummy double`}.1").asDouble()
                )

                assertEquals(
                    "svartekst",
                    førsteSvarelement.hentGeneratorSvar("${DummySeksjon.`generator dummy tekst`}.1").asText()
                )

                assertEquals(
                    1.april,
                    førsteSvarelement.hentGeneratorSvar("${DummySeksjon.`generator dummy localdate`}.1").asLocalDate()
                )

                assertEquals(
                    Periode(1.mai(), 1.juni()),
                    førsteSvarelement.hentGeneratorSvar("${DummySeksjon.`generator dummy periode`}.1").asPeriode()
                )
            }
        }
    }

    fun generatorsvar(vararg pairs: Pair<String, Any>): List<List<Pair<String, Any>>> =
        listOf(
            pairs.toList()
        )

    private val søknadUUID = UUID.randomUUID()

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
