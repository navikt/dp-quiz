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
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.periode
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.tekst
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.helpers.testPerson
import no.nav.dagpenger.model.helpers.testversjon
import no.nav.dagpenger.model.marshalling.SøkerJsonBuilder
import no.nav.dagpenger.model.regel.er
import no.nav.dagpenger.model.seksjon.Seksjon
import no.nav.dagpenger.model.seksjon.Søknadprosess
import no.nav.dagpenger.model.seksjon.Versjon
import no.nav.dagpenger.model.subsumsjon.Subsumsjon
import no.nav.dagpenger.model.subsumsjon.alle
import no.nav.dagpenger.model.subsumsjon.deltre
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class SøknadsmalVisitorJsonBuilderTest {
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
                "seksjon1",
                Rolle.søker,
                prototypeSøknad.boolsk(1),
                prototypeSøknad.heltall(2),
                prototypeSøknad.tekst(15),
                prototypeSøknad.periode(16),
                prototypeSøknad.periode(17)
            ),
            Seksjon(
                "seksjon2",
                Rolle.søker,
                prototypeSøknad.heltall(3),
                prototypeSøknad.dato(4),
                prototypeSøknad.generator(5),
                prototypeSøknad.desimaltall(6),
            ),
            Seksjon(
                "seksjon3",
                Rolle.søker,
                prototypeSøknad.flervalg(10),
                prototypeSøknad.envalg(11),
                prototypeSøknad.dato(12),
                prototypeSøknad.inntekt(13),
                prototypeSøknad.generator(14),
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

        val malJson = SøkerJsonBuilder(søknadprosess).resultat()

        assertEquals(0, malJson["versjon_id"].asInt())
        assertEquals("test", malJson["versjon_navn"].asText())
        assertEquals(4, malJson["seksjoner"].size())

        val førsteSeksjon = malJson["seksjoner"][0]
        assertEquals("seksjon1", førsteSeksjon["beskrivendeId"].asText())
        assertEquals(5, førsteSeksjon["fakta"].size())
        førsteSeksjon["fakta"][0].assertFaktaAsJson("1", "boolean", "boolsk1", listOf("søker"))
        førsteSeksjon["fakta"][1].assertFaktaAsJson("2", "int", "heltall2", listOf("søker"))
        førsteSeksjon["fakta"][2].assertFaktaAsJson("15", "tekst", "tekst15", listOf("søker"))
        førsteSeksjon["fakta"][3].assertFaktaAsJson("16", "periode", "periode16", listOf("søker"))
        førsteSeksjon["fakta"][4].assertFaktaAsJson("17", "periode", "pågåendePeriode17", listOf("søker"))

        val andreSeksjon = malJson["seksjoner"][1]
        assertEquals("seksjon2", andreSeksjon["beskrivendeId"].asText())
        assertEquals(2, andreSeksjon["fakta"].size())
        andreSeksjon["fakta"][0].assertFaktaAsJson("6", "double", "desimaltall6", listOf("søker"))
        andreSeksjon["fakta"][1].assertGeneratorFaktaAsJson(
            "5", "generator", "generator5", listOf("søker"),
            assertTemplates = listOf(
                { it.assertFaktaAsJson("3", "int", "heltall3", listOf("søker")) },
                { it.assertFaktaAsJson("4", "localdate", "dato4", listOf("søker")) }
            )
        )

        val tredjeSeksjon = malJson["seksjoner"][2]
        assertEquals("seksjon3", tredjeSeksjon["beskrivendeId"].asText())
        assertEquals(3, tredjeSeksjon["fakta"].size())
        tredjeSeksjon["fakta"][0].assertValgFaktaAsJson(
            "10",
            "flervalg",
            "flervalg10",
            listOf("søker"),
            listOf("valg1", "valg2", "valg3")
        )
        tredjeSeksjon["fakta"][1].assertValgFaktaAsJson(
            "11",
            "envalg",
            "envalg11",
            listOf("søker"),
            listOf("valg1", "valg2", "valg3")
        )
        tredjeSeksjon["fakta"][2].assertGeneratorFaktaAsJson(
            "14", "generator", "generator14", listOf("søker"),
            assertTemplates = listOf(
                { it.assertFaktaAsJson("12", "localdate", "dato12", listOf("søker")) },
                { it.assertFaktaAsJson("13", "inntekt", "inntekt13", listOf("søker")) }
            )
        )

        val fjerdeSeksjon = malJson["seksjoner"][3]
        assertEquals("nav", fjerdeSeksjon["beskrivendeId"].asText())
        assertEquals(3, fjerdeSeksjon["fakta"].size())
        fjerdeSeksjon["fakta"][0].assertFaktaAsJson("7", "dokument", "dokument7", listOf("nav"))
        fjerdeSeksjon["fakta"][1].assertFaktaAsJson("8", "inntekt", "inntekt8", listOf("nav"))
        fjerdeSeksjon["fakta"][2].assertFaktaAsJson("9", "localdate", "dato9", listOf("nav"))
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
            assertEquals(
                expectedRoller.size,
                actual.size,
                "$expectedBeskrivendeId har $actual, forventet $expectedRoller "
            )
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
