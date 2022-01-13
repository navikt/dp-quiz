package no.nav.dagpenger.model.unit.marshalling

import com.fasterxml.jackson.databind.JsonNode
import no.nav.dagpenger.model.factory.BaseFaktumFactory
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
import java.time.LocalDateTime
import java.util.UUID
import kotlin.test.assertEquals

internal class FaktaJsonBuilderTest {
    private lateinit var prototypeSøknad: Søknad

    @BeforeEach
    fun setup() {
        prototypeSøknad = Søknad(
            testversjon,
            BaseFaktumFactory.Companion.boolsk faktum "boolsk1" id 1,
            BaseFaktumFactory.Companion.heltall faktum "heltall2" id 2,
            BaseFaktumFactory.Companion.desimaltall faktum "desimaltall3" id 3,
            BaseFaktumFactory.Companion.dokument faktum "dokument4" id 4,
            BaseFaktumFactory.Companion.inntekt faktum "inntekt5" id 5,
            BaseFaktumFactory.Companion.dato faktum "dato6" id 6,
            BaseFaktumFactory.Companion.flervalg faktum "flervalg7" med "valg1" med "valg2" med "valg3" id 7,
            BaseFaktumFactory.Companion.envalg faktum "envalg8" med "valg1" med "valg2" id 8
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
    fun `serialisering til json`() {

        val regel = søkerSubsumsjon()
        val søknadprosess = søknadprosess(regel)

        val søkerJson = FaktaJsonBuilder(søknadprosess).resultat()

        assertEquals("NySøknad", søkerJson["@event_name"].asText())
        assertNull(søkerJson["@løsning"])
        assertDoesNotThrow { søkerJson["@id"].asText().also { UUID.fromString(it) } }
        assertDoesNotThrow { søkerJson["@opprettet"].asText().also { LocalDateTime.parse(it) } }
        assertDoesNotThrow { søkerJson["søknad_uuid"].asText().also { UUID.fromString(it) } }
        assertEquals("12020052345", søkerJson["fødselsnummer"].asText())
        assertEquals(8, søkerJson["fakta"].size())
        søkerJson["fakta"][0].assertFaktaAsJson(1, "boolean", "boolsk1", listOf("søker"))
        søkerJson["fakta"][1].assertFaktaAsJson(2, "int", "heltall2", listOf("søker"))
        søkerJson["fakta"][2].assertFaktaAsJson(3, "double", "desimaltall3", listOf("søker"))
        søkerJson["fakta"][3].assertFaktaAsJson(4, "dokument", "dokument4", listOf("nav"))
        søkerJson["fakta"][4].assertFaktaAsJson(5, "inntekt", "inntekt5", listOf("nav"))
        søkerJson["fakta"][5].assertFaktaAsJson(6, "localdate", "dato6", listOf("nav"))
        søkerJson["fakta"][6].assertValgFaktaAsJson(7, "flervalg", "flervalg7", listOf("søker"), listOf("valg1", "valg2", "valg3"))
        søkerJson["fakta"][7].assertValgFaktaAsJson(8, "envalg", "envalg8", listOf("søker"), listOf("valg1", "valg2"))
        assertEquals("1", søkerJson["fakta"][0]["id"].asText())
        assertEquals("2", søkerJson["fakta"][1]["id"].asText())
        assertEquals("3", søkerJson["fakta"][2]["id"].asText())
    }

    private fun JsonNode.assertFaktaAsJson(
        expectedId: Int,
        expectedClass: String,
        expectedNavn: String,
        expectedRoller: List<String>
    ) {
        assertEquals(expectedId, this.get("id").asInt())
        assertEquals(expectedClass, this.get("clazz").asText())
        assertEquals(expectedNavn, this.get("navn").asText())
        val actual: List<String> = this.get("roller").toSet().map { it.asText() }
        assertEquals(expectedRoller.size, actual.size)
        assertTrue(expectedRoller.containsAll<String>(actual))
    }

    private fun JsonNode.assertValgFaktaAsJson(
        expectedId: Int,
        expectedClass: String,
        expectedNavn: String,
        expectedRoller: List<String>,
        expectedGyldigeValg: List<String>
    ) {
        this.assertFaktaAsJson(expectedId, expectedClass, expectedNavn, expectedRoller)
        val actual: List<String> = this.get("gyldigeValg").toSet().map { it.asText() }
        assertTrue(expectedGyldigeValg.containsAll<String>(actual))
    }
}
