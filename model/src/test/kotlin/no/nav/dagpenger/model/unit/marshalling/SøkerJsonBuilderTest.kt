package no.nav.dagpenger.model.unit.marshalling

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.node.ObjectNode
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
import no.nav.dagpenger.model.regel.med
import no.nav.dagpenger.model.regel.under
import no.nav.dagpenger.model.regel.utfylt
import no.nav.dagpenger.model.seksjon.Seksjon
import no.nav.dagpenger.model.seksjon.Søknadprosess
import no.nav.dagpenger.model.seksjon.Versjon
import no.nav.dagpenger.model.subsumsjon.Subsumsjon
import no.nav.dagpenger.model.subsumsjon.alle
import no.nav.dagpenger.model.subsumsjon.deltre
import no.nav.dagpenger.model.subsumsjon.hvisIkkeOppfylt
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.UUID
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
            heltall faktum "f6" id 6,
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
    fun `SøkerJsonBuilder returnerer besvarte fakta og neste ubesvarte faktum`() {

        val regel = søkerSubsumsjon()
        val søknadprosess = søknadprosess(regel)

        SøkerJsonBuilder(søknadprosess).resultat().also {
            assertMetadata(it)
            assertAntallSeksjoner(1, it)
            assertUbesvartFaktum("seksjon1", it)
        }

        søknadprosess.boolsk(1).besvar(true)
        SøkerJsonBuilder(søknadprosess).resultat().also {
            assertAntallSeksjoner(1, it)
            assertUbesvartFaktum("seksjon1", it)
            assertBesvarteFakta(1, "seksjon1", it)
        }

        søknadprosess.boolsk(3).besvar(true)
        SøkerJsonBuilder(søknadprosess).resultat().also {
            assertAntallSeksjoner(1, it)
            assertUbesvartFaktum("seksjon1", it)
            assertBesvarteFakta(2, "seksjon1", it)
        }

        søknadprosess.boolsk(5).besvar(true)
        SøkerJsonBuilder(søknadprosess).resultat().also {
            assertAntallSeksjoner(2, it)
            assertBesvarteFakta(3, "seksjon1", it)
            assertUbesvartFaktum("seksjon2", it)
        }

        søknadprosess.generator(67).besvar(2)
        søknadprosess.heltall("6.1").besvar(17)
        søknadprosess.heltall("7.1").besvar(15)
        SøkerJsonBuilder(søknadprosess).resultat().also {
            println(it.toPrettyJson())
            assertAntallSeksjoner(2, it)
            assertBesvarteFakta(1, "seksjon2", it)
            assertUbesvartFaktum("seksjon2", it)
        }
    }

    private fun assertUbesvartFaktum(seksjon: String, søkerJson: ObjectNode) {
        assertEquals(
            1,
            søkerJson["seksjoner"].find { it["beskrivendeId"].asText() == seksjon }?.get("fakta")?.filterNot {
                it.has("svar")
            }?.size
        )
    }

    private fun assertBesvarteFakta(forventetAntall: Int, seksjon: String, søkerJson: ObjectNode) {
        assertEquals(
            forventetAntall,
            søkerJson["seksjoner"].find { it["beskrivendeId"].asText() == seksjon }?.get("fakta")?.filter {
                it.has("svar")
            }?.size
        )
    }

    private fun assertAntallSeksjoner(forventetAntall: Int, søkerJson: ObjectNode) {
        assertEquals(forventetAntall, søkerJson["seksjoner"].size())
    }

    private fun assertMetadata(søkerJson: ObjectNode) {
        assertEquals("Søkeroppgave", søkerJson["@event_name"].asText())
        Assertions.assertDoesNotThrow { søkerJson["@id"].asText().also { UUID.fromString(it) } }
        Assertions.assertDoesNotThrow { søkerJson["@opprettet"].asText().also { LocalDateTime.parse(it) } }
        Assertions.assertDoesNotThrow { søkerJson["søknad_uuid"].asText().also { UUID.fromString(it) } }
        assertEquals("12020052345", søkerJson["fødselsnummer"].asText())
    }

    private val objectMapper = jacksonMapperBuilder()
        .enable(SerializationFeature.INDENT_OUTPUT)
        .build()

    private fun String.toPrettyJson(): String? {
        val jsonNode = objectMapper.readValue<JsonNode>(this)
        return jsonNode.toPrettyJson()
    }

    private fun JsonNode.toPrettyJson() = objectMapper.writeValueAsString(this)

    private fun søkerSubsumsjon(): Subsumsjon {
        val alleBarnMåværeUnder18år = prototypeSøknad.heltall(6) under 18
        val deltre = "§ 1.2 har kun ikke myndige barn".deltre {
            alleBarnMåværeUnder18år.hvisIkkeOppfylt {
                prototypeSøknad.boolsk(7).utfylt()
            }
        }
        val generatorSubsumsjon67 = prototypeSøknad.generator(67) med deltre

        val regeltre = "regel" deltre {
            "alle i søknaden skal være besvart".alle(
                "alle i seksjon 1".alle(
                    (prototypeSøknad.boolsk(1) er true).hvisIkkeOppfylt {
                        prototypeSøknad.boolsk(2).utfylt()
                    },
                    (prototypeSøknad.boolsk(3) er true).hvisIkkeOppfylt {
                        prototypeSøknad.boolsk(4).utfylt()
                    },
                    prototypeSøknad.boolsk(5).utfylt()
                ),
                "alle i seksjon 2".alle(
                    generatorSubsumsjon67
                )
            )
        }
        return regeltre
    }

    private fun søknadprosess(prototypeSubsumsjon: Subsumsjon): Søknadprosess {
        val seksjoner = listOf(
            Seksjon(
                "seksjon1",
                Rolle.søker,
                prototypeSøknad.boolsk(1),
                prototypeSøknad.boolsk(2),
                prototypeSøknad.boolsk(3),
                prototypeSøknad.boolsk(4),
                prototypeSøknad.boolsk(5),
            ),
            Seksjon(
                "seksjon2",
                Rolle.søker,
                prototypeSøknad.heltall(6),
                prototypeSøknad.boolsk(7),
                prototypeSøknad.heltall(67),
            )
        )
        val prototypeFaktagrupper = Søknadprosess(
            prototypeSøknad,
            seksjoner = seksjoner.toTypedArray(),
            rootSubsumsjon = prototypeSubsumsjon
        )

        return Versjon.Bygger(
            prototypeSøknad,
            prototypeSubsumsjon,
            mapOf(Versjon.UserInterfaceType.Web to prototypeFaktagrupper)
        ).søknadprosess(testPerson, Versjon.UserInterfaceType.Web)
    }
}
