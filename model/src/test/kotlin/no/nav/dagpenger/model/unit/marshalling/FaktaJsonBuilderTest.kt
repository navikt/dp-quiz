package no.nav.dagpenger.model.unit.marshalling

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.NullNode
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.boolsk
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.dato
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.desimaltall
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.dokument
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.envalg
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.flervalg
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.heltall
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.inntekt
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.periode
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.tekst
import no.nav.dagpenger.model.faktum.Dokument
import no.nav.dagpenger.model.faktum.Envalg
import no.nav.dagpenger.model.faktum.Flervalg
import no.nav.dagpenger.model.faktum.Inntekt
import no.nav.dagpenger.model.faktum.Inntekt.Companion.årlig
import no.nav.dagpenger.model.faktum.Periode
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.faktum.Tekst
import no.nav.dagpenger.model.helpers.februar
import no.nav.dagpenger.model.helpers.januar
import no.nav.dagpenger.model.helpers.mars
import no.nav.dagpenger.model.helpers.testPerson
import no.nav.dagpenger.model.helpers.testversjon
import no.nav.dagpenger.model.marshalling.FaktaJsonBuilder
import no.nav.dagpenger.model.regel.er
import no.nav.dagpenger.model.seksjon.Seksjon
import no.nav.dagpenger.model.seksjon.Søknadprosess
import no.nav.dagpenger.model.seksjon.Versjon
import no.nav.dagpenger.model.subsumsjon.Subsumsjon
import no.nav.dagpenger.model.subsumsjon.alle
import no.nav.dagpenger.model.subsumsjon.deltre
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID
import kotlin.test.assertEquals

internal class FaktaJsonBuilderTest {
    private lateinit var prototypeSøknad: Søknad

    @BeforeEach
    fun setup() {
        prototypeSøknad = Søknad(
            testversjon,
            boolsk faktum "boolsk1" id 1,
            heltall faktum "heltall2" id 2,
            heltall faktum "heltall3" id 3,
            dato faktum "dato4" id 4,
            heltall faktum "generator5" id 5 genererer 3 og 4,
            desimaltall faktum "desimaltall6" id 6,
            dokument faktum "dokument7" id 7,
            inntekt faktum "inntekt8" id 8,
            dato faktum "dato9" id 9,
            flervalg faktum "flervalg10" med "valg1" med "valg2" med "valg3" id 10,
            envalg faktum "envalg11" med "valg1" med "valg2" id 11,
            dato faktum "dato12" id 12,
            inntekt faktum "inntekt13" id 13,
            heltall faktum "generator14" id 14 genererer 12 og 13,
            tekst faktum "tekst15" id 15,
            periode faktum "periode16" id 16,
            periode faktum "pågåendePeriode17" id 17
        )
    }

    private fun søknadprosess(prototypeSubsumsjon: Subsumsjon): Søknadprosess {
        val prototypeFaktagrupper = Søknadprosess(
            prototypeSøknad,
            Seksjon(
                "søker",
                Rolle.søker,
                prototypeSøknad.boolsk(1),
                prototypeSøknad.heltall(2),
                prototypeSøknad.heltall(3),
                prototypeSøknad.dato(4),
                prototypeSøknad.generator(5),
                prototypeSøknad.desimaltall(6),
                prototypeSøknad.flervalg(10),
                prototypeSøknad.envalg(11),
                prototypeSøknad.dato(12),
                prototypeSøknad.inntekt(13),
                prototypeSøknad.generator(14),
                prototypeSøknad.tekst(15),
                prototypeSøknad.periode(16),
                prototypeSøknad.periode(17)
            ),
            Seksjon(
                "nav", Rolle.nav,
                prototypeSøknad.dokument(7),
                prototypeSøknad.inntekt(8),
                prototypeSøknad.dato(9)
            ),
            rootSubsumsjon = prototypeSubsumsjon
        )

        return Versjon.Bygger(
            prototypeSøknad,
            prototypeSubsumsjon,
            mapOf(Versjon.UserInterfaceType.Web to prototypeFaktagrupper)
        ).søknadprosess(testPerson, Versjon.UserInterfaceType.Web)
    }

    private fun søkerSubsumsjon() = "regel" deltre {
        "alle".alle(
            prototypeSøknad.boolsk(1) er true
        )
    }

    @Test
    fun `serialisering av ubesvarte fakta til json`() {

        val regel = søkerSubsumsjon()
        val søknadprosess = søknadprosess(regel)

        val søkerJson = FaktaJsonBuilder(søknadprosess).resultat()

        assertEquals("NySøknad", søkerJson["@event_name"].asText())
        assertNull(søkerJson["@løsning"])
        assertDoesNotThrow { søkerJson["@id"].asText().also { UUID.fromString(it) } }
        assertDoesNotThrow { søkerJson["@opprettet"].asText().also { LocalDateTime.parse(it) } }
        assertDoesNotThrow { søkerJson["søknad_uuid"].asText().also { UUID.fromString(it) } }
        assertEquals("12020052345", søkerJson["fødselsnummer"].asText())
        assertEquals(13, søkerJson["fakta"].size())
        søkerJson["fakta"][0].assertFaktaAsJson("1", "boolean", "boolsk1", listOf("søker"))
        søkerJson["fakta"][1].assertFaktaAsJson("2", "int", "heltall2", listOf("søker"))
        søkerJson["fakta"][2].assertFaktaAsJson("6", "double", "desimaltall6", listOf("søker"))
        søkerJson["fakta"][3].assertFaktaAsJson("7", "dokument", "dokument7", listOf("nav"))
        søkerJson["fakta"][4].assertFaktaAsJson("8", "inntekt", "inntekt8", listOf("nav"))
        søkerJson["fakta"][5].assertFaktaAsJson("9", "localdate", "dato9", listOf("nav"))
        søkerJson["fakta"][6].assertValgFaktaAsJson(
            "10",
            "flervalg",
            "flervalg10",
            listOf("søker"),
            listOf("valg1", "valg2", "valg3")
        )
        søkerJson["fakta"][7].assertValgFaktaAsJson("11", "envalg", "envalg11", listOf("søker"), listOf("valg1", "valg2"))
        søkerJson["fakta"][8].assertFaktaAsJson("15", "tekst", "tekst15", listOf("søker"))
        søkerJson["fakta"][9].assertFaktaAsJson("16", "periode", "periode16", listOf("søker"))
        søkerJson["fakta"][10].assertFaktaAsJson("17", "periode", "pågåendePeriode17", listOf("søker"))

        søkerJson["fakta"][11].assertGeneratorFaktaAsJson(
            "5", "generator", "generator5", listOf("søker"),
            assertTemplates = listOf(
                { it.assertFaktaAsJson("3", "int", "heltall3", listOf("søker")) },
                { it.assertFaktaAsJson("4", "localdate", "dato4", listOf("søker")) }
            )
        )

        søkerJson["fakta"][12].assertGeneratorFaktaAsJson(
            "14", "generator", "generator14", listOf("søker"),
            assertTemplates = listOf(
                { it.assertFaktaAsJson("12", "localdate", "dato12", listOf("søker")) },
                { it.assertFaktaAsJson("13", "inntekt", "inntekt13", listOf("søker")) }
            )
        )
    }

    @Test
    fun `serialisering av besvarte fakta til json`() {

        val nå = LocalDateTime.now()
        val idag = LocalDate.now()
        val regel = søkerSubsumsjon()
        val søknadprosess = søknadprosess(regel)
        søknadprosess.boolsk(1).besvar(true)
        søknadprosess.heltall(2).besvar(37)

        søknadprosess.generator(5).besvar(2)
        søknadprosess.heltall("3.1").besvar(37)
        søknadprosess.heltall("3.2").besvar(100)
        søknadprosess.dato("4.1").besvar(idag)
        søknadprosess.dato("4.2").besvar(idag.plusDays(3))

        søknadprosess.desimaltall(6).besvar(37.5)

        søknadprosess.dokument(7).besvar(Dokument(url = "urn:dp:dokument", lastOppTidsstempel = nå))

        søknadprosess.inntekt(8).besvar(Inntekt.INGEN)
        søknadprosess.dato(9).besvar(idag)
        søknadprosess.flervalg(10).besvar(Flervalg("flervalg10.valg1"))
        søknadprosess.envalg(11).besvar(Envalg("envalg11.valg1"))
        søknadprosess.generator(14).besvar(1)
        søknadprosess.dato("12.1").besvar(idag)
        søknadprosess.inntekt("13.1").besvar(300.årlig)
        søknadprosess.tekst(15).besvar(Tekst("svartekst15"))
        søknadprosess.periode(16).besvar(Periode(1.januar(), 1.februar()))
        søknadprosess.periode(17).besvar(Periode(1.mars()))

        val søkerJson = FaktaJsonBuilder(søknadprosess).resultat()

        assertEquals("NySøknad", søkerJson["@event_name"].asText())
        assertNull(søkerJson["@løsning"])
        assertDoesNotThrow { søkerJson["@id"].asText().also { UUID.fromString(it) } }
        assertDoesNotThrow { søkerJson["@opprettet"].asText().also { LocalDateTime.parse(it) } }
        assertDoesNotThrow { søkerJson["søknad_uuid"].asText().also { UUID.fromString(it) } }
        assertEquals("12020052345", søkerJson["fødselsnummer"].asText())
        assertEquals(13, søkerJson["fakta"].size())

        søkerJson["fakta"][0].assertFaktaAsJson("1", "boolean", "boolsk1", listOf("søker")) {
            assertEquals(true, it.asBoolean())
        }
        søkerJson["fakta"][1].assertFaktaAsJson("2", "int", "heltall2", listOf("søker")) {
            assertEquals(37, it.asInt())
        }
        søkerJson["fakta"][2].assertFaktaAsJson("6", "double", "desimaltall6", listOf("søker")) {
            assertEquals(37.5, it.asDouble())
        }
        søkerJson["fakta"][3].assertFaktaAsJson("7", "dokument", "dokument7", listOf("nav")) {
            assertEquals("urn:dp:dokument", it["url"].asText())
            assertEquals(nå, it["lastOppTidsstempel"].asText().let { LocalDateTime.parse(it) })
        }
        søkerJson["fakta"][4].assertFaktaAsJson("8", "inntekt", "inntekt8", listOf("nav")) {
            assertEquals(Inntekt.INGEN, it.asDouble().årlig)
        }
        søkerJson["fakta"][5].assertFaktaAsJson("9", "localdate", "dato9", listOf("nav")) {
            assertEquals(idag, it.asText().let { LocalDate.parse(it) })
        }
        søkerJson["fakta"][6].assertValgFaktaAsJson(
            "10",
            "flervalg",
            "flervalg10",
            listOf("søker"),
            listOf("valg1", "valg2", "valg3")
        ) {
            assertEquals(Flervalg("flervalg10.valg1"), Flervalg(it.map { it.asText() }.toSet()))
        }
        søkerJson["fakta"][7].assertValgFaktaAsJson("11", "envalg", "envalg11", listOf("søker"), listOf("valg1", "valg2")) {
            assertEquals(Envalg("envalg11.valg1"), Envalg(it.asText()))
        }

        søkerJson["fakta"][8].assertFaktaAsJson("15", "tekst", "tekst15", listOf("søker")) {
            assertEquals("svartekst15", it.asText())
        }

        søkerJson["fakta"][9].assertFaktaAsJson("16", "periode", "periode16", listOf("søker")) {
            assertEquals("2018-01-01", it["fom"].asText())
            assertEquals("2018-02-01", it["tom"].asText())
        }

        søkerJson["fakta"][10].assertFaktaAsJson("17", "periode", "pågåendePeriode17", listOf("søker")) {
            assertEquals("2018-03-01", it["fom"].asText())
            assertTrue(it["tom"] is NullNode)
        }

        søkerJson["fakta"][11].assertGeneratorFaktaAsJson(
            "5", "generator", "generator5", listOf("søker"),
            assertTemplates = listOf(
                { it.assertFaktaAsJson("3", "int", "heltall3", listOf("søker")) },
                { it.assertFaktaAsJson("4", "localdate", "dato4", listOf("søker")) }
            )
        ) { svar ->
            assertEquals(2, svar.size())

            assertEquals(37, svar[0]["heltall3"].asInt())
            assertEquals(idag, svar[0]["dato4"].asText().let { LocalDate.parse(it) })
            assertEquals(100, svar[1]["heltall3"].asInt())
            assertEquals(idag.plusDays(3), svar[1]["dato4"].asText().let { LocalDate.parse(it) })
        }

        søkerJson["fakta"][12].assertGeneratorFaktaAsJson(
            "14", "generator", "generator14", listOf("søker"),
            assertTemplates = listOf(
                { it.assertFaktaAsJson("12", "localdate", "dato12", listOf("søker")) },
                { it.assertFaktaAsJson("13", "inntekt", "inntekt13", listOf("søker")) }
            )
        ) { svar ->
            assertEquals(1, svar.size())
            assertEquals(idag, svar[0]["dato12"].asText().let { LocalDate.parse(it) })
            assertEquals(300.årlig, svar[0]["inntekt13"].asDouble().årlig)
        }
    }

    private fun JsonNode.assertFaktaAsJson(
        expectedId: String,
        expectedType: String,
        expectedBeskrivendeId: String,
        expectedRoller: List<String>,
        assertSvar: ((JsonNode) -> Unit)? = null
    ) {
        assertEquals(expectedBeskrivendeId, this.get("beskrivendeId").asText())
        assertEquals(expectedType, this.get("type").asText())
        assertEquals(expectedId, this.get("id").asText())
        if (expectedRoller.isNotEmpty()) {
            val actual: List<String> = this.get("roller").toSet().map { it.asText() }
            assertEquals(expectedRoller.size, actual.size, "$expectedBeskrivendeId har $actual, forventet $expectedRoller ")
            assertTrue(expectedRoller.containsAll<String>(actual)) { "$expectedBeskrivendeId har $actual, forventet $expectedRoller " }
        }
        assertSvar?.let { assert -> assert(this.get("svar")) }
    }

    private fun JsonNode.assertGeneratorFaktaAsJson(
        expectedId: String,
        expectedType: String,
        expectedBeskrivendeId: String,
        expectedRoller: List<String>,
        assertTemplates: List<(JsonNode) -> Unit>,
        assertSvar: ((JsonNode) -> Unit)? = null
    ) {
        this.assertFaktaAsJson(expectedId, expectedType, expectedBeskrivendeId, expectedRoller, assertSvar)
        assertTemplates.forEachIndexed { index: Int, test: (JsonNode) -> Unit ->
            test(this.get("templates")[index])
        }
    }

    private fun JsonNode.assertValgFaktaAsJson(
        expectedId: String,
        expectedClass: String,
        expectedNavn: String,
        expectedRoller: List<String>,
        expectedGyldigeValg: List<String>,
        assertSvar: ((JsonNode) -> Unit)? = null
    ) {
        this.assertFaktaAsJson(expectedId, expectedClass, expectedNavn, expectedRoller, assertSvar)
        val expectedGyldigeValgMedPrefix = expectedGyldigeValg.map { "$expectedNavn.$it" }
        val actual: List<String> = this.get("gyldigeValg").toSet().map { it.asText() }
        assertTrue(expectedGyldigeValgMedPrefix.containsAll<String>(actual))
    }
}
