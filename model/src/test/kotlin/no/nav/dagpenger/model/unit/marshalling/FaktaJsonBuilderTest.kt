package no.nav.dagpenger.model.unit.marshalling

import com.fasterxml.jackson.databind.JsonNode
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.boolsk
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.dato
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.desimaltall
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.dokument
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.envalg
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.flervalg
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.heltall
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.inntekt
import no.nav.dagpenger.model.faktum.Dokument
import no.nav.dagpenger.model.faktum.Envalg
import no.nav.dagpenger.model.faktum.Flervalg
import no.nav.dagpenger.model.faktum.Inntekt
import no.nav.dagpenger.model.faktum.Inntekt.Companion.årlig
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.faktum.Søknad
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
            desimaltall faktum "desimaltall3" id 3,
            dokument faktum "dokument4" id 4,
            inntekt faktum "inntekt5" id 5,
            dato faktum "dato6" id 6,
            flervalg faktum "flervalg7" med "valg1" med "valg2" med "valg3" id 7,
            envalg faktum "envalg8" med "valg1" med "valg2" id 8,
            dato faktum "dato9" id 9,
            inntekt faktum "inntekt10" id 10,
            heltall faktum "generator11" id 11 genererer 9 og 10
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
                prototypeSøknad.desimaltall(3),
                prototypeSøknad.flervalg(7),
                prototypeSøknad.envalg(8),
                prototypeSøknad.dato(9),
                prototypeSøknad.inntekt(10),
                prototypeSøknad.generator(11)
            ),
            Seksjon(
                "nav", Rolle.nav,
                prototypeSøknad.dokument(4),
                prototypeSøknad.inntekt(5),
                prototypeSøknad.dato(6)
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
        assertEquals(11, søkerJson["fakta"].size())
        søkerJson["fakta"][0].assertFaktaAsJson("1", "boolean", "boolsk1", listOf("søker"))
        søkerJson["fakta"][1].assertFaktaAsJson("2", "int", "heltall2", listOf("søker"))
        søkerJson["fakta"][2].assertFaktaAsJson("3", "double", "desimaltall3", listOf("søker"))
        søkerJson["fakta"][3].assertFaktaAsJson("4", "dokument", "dokument4", listOf("nav"))
        søkerJson["fakta"][4].assertFaktaAsJson("5", "inntekt", "inntekt5", listOf("nav"))
        søkerJson["fakta"][5].assertFaktaAsJson("6", "localdate", "dato6", listOf("nav"))
        søkerJson["fakta"][6].assertValgFaktaAsJson(
            "7",
            "flervalg",
            "flervalg7",
            listOf("søker"),
            listOf("valg1", "valg2", "valg3")
        )
        søkerJson["fakta"][7].assertValgFaktaAsJson("8", "envalg", "envalg8", listOf("søker"), listOf("valg1", "valg2"))
        søkerJson["fakta"][8].assertFaktaAsJson("9", "localdate", "dato9", listOf("søker"))
        søkerJson["fakta"][9].assertFaktaAsJson("10", "inntekt", "inntekt10", listOf("søker"))
        søkerJson["fakta"][10].assertGeneratorFaktaAsJson(
            "11", "generator", "generator11", listOf("søker"),
            assertTemplates = listOf(
                { it.assertFaktaAsJson("9", "localdate", "dato9", listOf("søker")) },
                { it.assertFaktaAsJson("10", "inntekt", "inntekt10", listOf("søker")) }
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
        søknadprosess.desimaltall(3).besvar(37.5)
        søknadprosess.dokument(4).besvar(Dokument(url = "urn:dp:dokument", lastOppTidsstempel = nå))
        søknadprosess.inntekt(5).besvar(Inntekt.INGEN)
        søknadprosess.dato(6).besvar(idag)
        søknadprosess.flervalg(7).besvar(Flervalg("valg1"))
        søknadprosess.envalg(8).besvar(Envalg("valg1"))
        søknadprosess.generator(11).besvar(1)
        søknadprosess.dato("9.1").besvar(LocalDate.now())
        søknadprosess.inntekt("10.1").besvar(300.årlig)

        val søkerJson = FaktaJsonBuilder(søknadprosess).resultat()

        assertEquals("NySøknad", søkerJson["@event_name"].asText())
        assertNull(søkerJson["@løsning"])
        assertDoesNotThrow { søkerJson["@id"].asText().also { UUID.fromString(it) } }
        assertDoesNotThrow { søkerJson["@opprettet"].asText().also { LocalDateTime.parse(it) } }
        assertDoesNotThrow { søkerJson["søknad_uuid"].asText().also { UUID.fromString(it) } }
        assertEquals("12020052345", søkerJson["fødselsnummer"].asText())
        assertEquals(9, søkerJson["fakta"].size())
        søkerJson["fakta"][0].assertFaktaAsJson("1", "boolean", "boolsk1", listOf("søker")) {
            assertEquals(true, it.asBoolean())
        }
        søkerJson["fakta"][1].assertFaktaAsJson("2", "int", "heltall2", listOf("søker")) {
            assertEquals(37, it.asInt())
        }
        søkerJson["fakta"][2].assertFaktaAsJson("3", "double", "desimaltall3", listOf("søker")) {
            assertEquals(37.5, it.asDouble())
        }
        søkerJson["fakta"][3].assertFaktaAsJson("4", "dokument", "dokument4", listOf("nav")) {
            assertEquals("urn:dp:dokument", it["url"].asText())
            assertEquals(nå, it["lastOppTidsstempel"].asText().let { LocalDateTime.parse(it) })
        }
        søkerJson["fakta"][4].assertFaktaAsJson("5", "inntekt", "inntekt5", listOf("nav")) {
            assertEquals(Inntekt.INGEN, it.asDouble().let { it.årlig })
        }
        søkerJson["fakta"][5].assertFaktaAsJson("6", "localdate", "dato6", listOf("nav")) {
            assertEquals(idag, it.asText().let { LocalDate.parse(it) })
        }
        søkerJson["fakta"][6].assertValgFaktaAsJson(
            "7",
            "flervalg",
            "flervalg7",
            listOf("søker"),
            listOf("valg1", "valg2", "valg3")
        ) {
            assertEquals(Flervalg("valg1"), Flervalg(it.map { it.asText() }.toSet()))
        }
        søkerJson["fakta"][7].assertValgFaktaAsJson(
            "8",
            "envalg",
            "envalg8",
            listOf("søker"),
            listOf("valg1", "valg2")
        ) {
            assertEquals(Envalg("valg1"), Envalg(it.asText()))
        }
        søkerJson["fakta"][8].assertGeneratorFaktaAsJson(
            "11", "generator", "generator11", listOf("søker"),
            assertTemplates = listOf(
                { it.assertFaktaAsJson("9", "localdate", "dato9", listOf("søker")) },
                { it.assertFaktaAsJson("10", "inntekt", "inntekt10", listOf("søker")) }
            ) {  svar ->
            }

        )
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
            assertEquals(expectedRoller.size, actual.size)
            assertTrue(expectedRoller.containsAll<String>(actual))
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
        val actual: List<String> = this.get("gyldigeValg").toSet().map { it.asText() }
        assertTrue(expectedGyldigeValg.containsAll<String>(actual))
    }
}
