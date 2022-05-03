package no.nav.dagpenger.model.unit.marshalling

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonMapperBuilder
import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.boolsk
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.dato
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.heltall
import no.nav.dagpenger.model.factory.UtledetFaktumFactory.Companion.maks
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.helpers.testPerson
import no.nav.dagpenger.model.helpers.testversjon
import no.nav.dagpenger.model.marshalling.SøkerJsonBuilder
import no.nav.dagpenger.model.regel.er
import no.nav.dagpenger.model.regel.utfylt
import no.nav.dagpenger.model.seksjon.Seksjon
import no.nav.dagpenger.model.seksjon.Søknadprosess
import no.nav.dagpenger.model.seksjon.Versjon
import no.nav.dagpenger.model.subsumsjon.Subsumsjon
import no.nav.dagpenger.model.subsumsjon.alle
import no.nav.dagpenger.model.subsumsjon.deltre
import no.nav.dagpenger.model.subsumsjon.hvisIkkeOppfylt
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class SøkerJsonBuilderTest {

    private lateinit var prototypeSøknad: Søknad

    @BeforeEach
    fun setup() {
        prototypeSøknad = Søknad(
            testversjon,
            boolsk faktum "f1" id 1,
            boolsk faktum "f2" id 2 avhengerAv 1,
            boolsk faktum "f3" id 3,
            boolsk faktum "f4" id 4 avhengerAv 3,
            boolsk faktum "f5" id 5,
            boolsk faktum "f6" id 6,
            boolsk faktum "f7" id 7,
            heltall faktum "f67" id 67 genererer 6 og 7,
            dato faktum "f8" id 8,
            dato faktum "f9" id 9,
            maks dato "f10" av 8 og 9 id 10,
            boolsk faktum "f11" id 11 avhengerAv 10,
            boolsk faktum "f12" id 12 avhengerAv 67,
            heltall faktum "f1314" id 1314 genererer 13 og 14,
            boolsk faktum "f13" id 13,
            boolsk faktum "f14" id 14,
        )
    }

    @Test
    fun `SøkerJsonBuilder inneholder riktig eventnavn og metadata`() {

        val regel = søkerSubsumsjon()
        val søknadprosess = søknadprosess(regel)
        søknadprosess.nesteSeksjoner()
        var søkerJson = SøkerJsonBuilder(søknadprosess).resultat()


        assertEquals(1, søkerJson["seksjoner"].size())
        assertEquals(0, søkerJson["seksjoner"][0]["fakta"].size())

        søknadprosess.boolsk(1).besvar(true)
        søkerJson = SøkerJsonBuilder(søknadprosess).resultat()
        println(søkerJson.toPrettyJson())
        val seksjoner = søkerJson["seksjoner"]
        val gjeldendeSeksjon = seksjoner[0]
        val gjeldendeFakta = gjeldendeSeksjon["fakta"]
        assertEquals(1, seksjoner.size())
        assertEquals(1, gjeldendeFakta.size())
        assertEquals(true, gjeldendeFakta[0]["svar"].asBoolean())

        /*
        assertEquals("søker_oppgave", søkerJson["@event_name"].asText())
        assertDoesNotThrow { søkerJson["@id"].asText().also { UUID.fromString(it) } }
        assertDoesNotThrow { søkerJson["@opprettet"].asText().also { LocalDateTime.parse(it) } }
        assertDoesNotThrow { søkerJson["søknad_uuid"].asText().also { UUID.fromString(it) } }
        assertEquals("søker", søkerJson["seksjon_navn"].asText())
        assertEquals(3, søkerJson["fakta"].size())
        assertEquals("1", søkerJson["fakta"][0]["id"].asText())
        assertEquals("3", søkerJson["fakta"][1]["id"].asText())
        assertEquals("5", søkerJson["fakta"][2]["id"].asText())
        assertNotNull(søkerJson["identer"])
        assertEquals("12020052345", søkerJson["identer"][0]["id"].asText())
        assertEquals("folkeregisterident", søkerJson["identer"][0]["type"].asText())
        assertEquals("aktørId", søkerJson["identer"][1]["id"].asText())
        assertEquals("aktørid", søkerJson["identer"][1]["type"].asText())
        assertNotNull(søkerJson["subsumsjoner"], "Skal ha med subsumsjon")
        assertEquals(1, søkerJson["subsumsjoner"].size()) */
    }

    private val objectMapper = jacksonMapperBuilder()
        .enable(SerializationFeature.INDENT_OUTPUT)
        .build()

    private fun String.toPrettyJson(): String? {
        val jsonNode = objectMapper.readValue<JsonNode>(this)
        return jsonNode.toPrettyJson()
    }

    private fun JsonNode.toPrettyJson() = objectMapper.writeValueAsString(this)

    private fun søkerSubsumsjon() = "regel" deltre {
        "alle i første seksjon".alle(
            (prototypeSøknad.boolsk(1) er true).hvisIkkeOppfylt {
                prototypeSøknad.boolsk(2).utfylt()
            },
            (prototypeSøknad.boolsk(3) er true).hvisIkkeOppfylt {
                prototypeSøknad.boolsk(4).utfylt()
            },
            prototypeSøknad.boolsk(5).utfylt()
        )
    }

    private fun søknadprosess(prototypeSubsumsjon: Subsumsjon): Søknadprosess {
        val prototypeFaktagrupper = Søknadprosess(
            prototypeSøknad,
            Seksjon(
                "førsteSeksjon",
                Rolle.søker,
                prototypeSøknad.boolsk(1),
                prototypeSøknad.boolsk(2),
                prototypeSøknad.boolsk(3),
                prototypeSøknad.boolsk(4),
                prototypeSøknad.boolsk(5),
            ),
            rootSubsumsjon = prototypeSubsumsjon
        )

        return Versjon.Bygger(
            prototypeSøknad,
            prototypeSubsumsjon,
            mapOf(Versjon.UserInterfaceType.Web to prototypeFaktagrupper)
        ).søknadprosess(testPerson, Versjon.UserInterfaceType.Web)
    }
}
