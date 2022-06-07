package no.nav.dagpenger.model.unit.marshalling

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.boolsk
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.dato
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.dokument
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.envalg
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.flervalg
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.heltall
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.land
import no.nav.dagpenger.model.factory.UtledetFaktumFactory.Companion.maks
import no.nav.dagpenger.model.faktum.Dokument
import no.nav.dagpenger.model.faktum.Envalg
import no.nav.dagpenger.model.faktum.Flervalg
import no.nav.dagpenger.model.faktum.Land
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.helpers.testPerson
import no.nav.dagpenger.model.helpers.testversjon
import no.nav.dagpenger.model.marshalling.SøkerJsonBuilder
import no.nav.dagpenger.model.regel.dokumenteresAv
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
import no.nav.dagpenger.model.subsumsjon.hvisOppfylt
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class SøkerJsonBuilderTest {

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
            dokument faktum "f15" id 15 avhengerAv 5,
            boolsk faktum "f16" id 16 avhengerAv 15,
            envalg faktum "f17" id 17 med "envalg1" med "envalg2",
            flervalg faktum "f18" id 18 med "flervalg1" med "flervalg2",
            heltall faktum "f1718" id 1718 genererer 17 og 18,
            land faktum "f19" id 19
        )
    }

    @Test
    fun `SøkerJsonBuilder returnerer besvarte fakta og neste ubesvarte faktum`() {

        val regel = søkerSubsumsjon()
        val søknadprosess = søknadprosess(regel)

        SøkerJsonBuilder(søknadprosess).resultat().also {
            assertMetadata(it)
            assertAntallSeksjoner(1, it)
            val fakta = it.finnSeksjon("seksjon1")["fakta"]
            fakta[0].assertFaktaAsJson("1", "boolean", "f1", listOf("søker"))
        }

        søknadprosess.boolsk(1).besvar(true)
        SøkerJsonBuilder(søknadprosess).resultat().also {
            assertAntallSeksjoner(1, it)

            assertAntallSeksjoner(1, it)
            val fakta = it.finnSeksjon("seksjon1")["fakta"]
            fakta[0].assertFaktaAsJson("1", "boolean", "f1", listOf("søker")) { svar ->
                assertEquals("true", svar.asText())
            }
            fakta[1].assertFaktaAsJson("3", "boolean", "f3", listOf("søker"))
        }

        søknadprosess.boolsk(3).besvar(true)
        SøkerJsonBuilder(søknadprosess).resultat().also {
            assertAntallSeksjoner(1, it)
            val fakta = it.finnSeksjon("seksjon1")["fakta"]
            fakta[0].assertFaktaAsJson("1", "boolean", "f1", listOf("søker")) { svar ->
                assertEquals("true", svar.asText())
            }
            fakta[1].assertFaktaAsJson("3", "boolean", "f3", listOf("søker")) { svar ->
                assertEquals("true", svar.asText())
            }
            fakta[2].assertFaktaAsJson("5", "boolean", "f5", listOf("søker"))
            assertUbesvartFaktum("seksjon1", it)
            assertBesvarteFakta(2, "seksjon1", it)
        }

        søknadprosess.boolsk(5).besvar(true)
        SøkerJsonBuilder(søknadprosess).resultat().also {
            assertAntallSeksjoner(2, it)
            val seksjon1Fakta = it.finnSeksjon("seksjon1")["fakta"]
            seksjon1Fakta[0].assertFaktaAsJson("1", "boolean", "f1", listOf("søker")) { svar ->
                assertEquals("true", svar.asText())
            }
            seksjon1Fakta[1].assertFaktaAsJson("3", "boolean", "f3", listOf("søker")) { svar ->
                assertEquals("true", svar.asText())
            }
            seksjon1Fakta[2].assertFaktaAsJson("5", "boolean", "f5", listOf("søker")) { svar ->
                assertEquals("true", svar.asText())
            }
            val seksjon2Fakta = it.finnSeksjon("seksjon2")["fakta"]
            seksjon2Fakta[0].assertGeneratorFaktaAsJson(
                "67", "generator", "f67", listOf("søker"),
                assertTemplates = listOf(
                    { it.assertFaktaAsJson("6", "int", "f6", listOf("søker")) },
                    { it.assertFaktaAsJson("7", "boolean", "f7", listOf("søker")) },
                )
            )
            assertBesvarteFakta(3, "seksjon1", it)
            assertUbesvartFaktum("seksjon2", it)
        }

        søknadprosess.generator(67).besvar(2)
        søknadprosess.heltall("6.1").besvar(21)
        søknadprosess.boolsk("7.1").besvar(true)
        SøkerJsonBuilder(søknadprosess).resultat().also {
            assertAntallSeksjoner(2, it)
            assertBesvarteFakta(1, "seksjon2", it)
            val seksjon2Fakta = it.finnSeksjon("seksjon2")["fakta"]
            seksjon2Fakta[0].assertGeneratorFaktaAsJson(
                "67", "generator", "f67", listOf("søker"),
                assertTemplates = listOf(
                    { it.assertFaktaAsJson("6", "int", "f6", listOf("søker")) },
                    { it.assertFaktaAsJson("7", "boolean", "f7", listOf("søker")) },
                )
            )
            assertBesvartGeneratorFaktum(2, "f67", "seksjon2", it)
            assertUbesvartGeneratorFaktum("f67", "seksjon2", it)
        }

        søknadprosess.heltall("6.2").besvar(19)
        SøkerJsonBuilder(søknadprosess).resultat().also {
            assertAntallSeksjoner(2, it)
            assertBesvarteFakta(1, "seksjon2", it)
            assertUbesvartGeneratorFaktum("f67", "seksjon2", it)
            assertBesvartGeneratorFaktum(3, "f67", "seksjon2", it)
        }

        søknadprosess.boolsk("7.2").besvar(true)
        SøkerJsonBuilder(søknadprosess).resultat().also {
            assertAntallSeksjoner(2, it)
            assertBesvarteFakta(1, "seksjon2", it)
            assertUbesvartGeneratorFaktum(
                forventetAntall = 0,
                generatorFaktumNavn = "f67",
                seksjon = "seksjon2",
                søkerJson = it
            )
            assertBesvartGeneratorFaktum(4, "f67", "seksjon2", it)
            assertIkkeFerdig(it)
        }

        søknadprosess.dato("8").besvar(LocalDate.now())
        søknadprosess.dato("9").besvar(LocalDate.now())

        SøkerJsonBuilder(søknadprosess).resultat().also {
            assertAntallSeksjoner(3, it)
            assertUbesvartFaktum("dokumentasjon", it)
        }

        søknadprosess.dokument(15).besvar(Dokument(LocalDate.now(), "urn:nav:1234"))

        søknadprosess.generator(1718).besvar(1)

        SøkerJsonBuilder(søknadprosess).resultat().also {
            assertAntallSeksjoner(4, it)
            val generatorFakta = it.finnSeksjon("seksjon3")["fakta"]
            generatorFakta[0].assertGeneratorFaktaAsJson(
                "1718", "generator", "f1718", listOf("søker"),
                assertTemplates = listOf(
                    {
                        it.assertValgFaktaAsJson(
                            "17",
                            "envalg",
                            "f17",
                            listOf("søker"),
                            expectedGyldigeValg = listOf("envalg1", "envalg2")
                        )
                    },
                    {
                        it.assertValgFaktaAsJson(
                            "18",
                            "flervalg",
                            "f18",
                            listOf("søker"),
                            expectedGyldigeValg = listOf("flervalg1", "flervalg2")
                        )
                    },
                )
            )
            assertUbesvartGeneratorFaktum(
                forventetAntall = 1,
                generatorFaktumNavn = "f1718",
                seksjon = "seksjon3",
                søkerJson = it
            )
        }

        søknadprosess.envalg("17.1").besvar(Envalg("f17.envalg1"))
        søknadprosess.flervalg("18.1").besvar(Flervalg("f18.flervalg2"))

        SøkerJsonBuilder(søknadprosess).resultat().also {
            assertAntallSeksjoner(5, it)
            val seksjonsFakta = it.finnSeksjon("Gyldige land")["fakta"]
            seksjonsFakta[0].assertLandFaktum(
                "19",
                "land",
                "f19",
                listOf("søker"),
            )
            assertUbesvartFaktum("Gyldige land", it)
        }

        søknadprosess.land(19).besvar(Land("NOR"))

        SøkerJsonBuilder(søknadprosess).resultat().also {
            assertFerdig(it)
        }
    }

    @Test
    fun `Boolske fakta skal ha en beskrivendeId for hvert av de to gyldige valgene`() {
        val regel = søkerSubsumsjon()
        val søknadprosess = søknadprosess(regel)

        SøkerJsonBuilder(søknadprosess).resultat().also { jsonUtenSvar ->
            jsonUtenSvar["seksjoner"][0]["fakta"][0].assertBoolskFaktumHarGyldigeValg("f1")
        }

        søknadprosess.boolsk(1).besvar(true)
        SøkerJsonBuilder(søknadprosess).resultat().also { jsonMedSvar ->
            assertBesvarteFakta(1, "seksjon1", jsonMedSvar)
            jsonMedSvar["seksjoner"][0]["fakta"][0].assertBoolskFaktumHarGyldigeValg("f1")
        }
    }

    private fun JsonNode.assertBoolskFaktumHarGyldigeValg(expectedBaseBeskrivendeId: String) {
        val expectedGyldigeValg = listOf("svar.ja", "svar.nei").map { "$expectedBaseBeskrivendeId.$it" }
        val actual: List<String> = this.get("gyldigeValg").toSet().map { it.asText() }
        assertEquals(2, actual.size)
        Assertions.assertTrue(expectedGyldigeValg.containsAll<String>(actual))
    }

    private fun søkerSubsumsjon(): Subsumsjon {
        val alleBarnMåværeUnder18år = prototypeSøknad.heltall(6) under 18
        val deltre = "§ 1.2 har kun ikke myndige barn".deltre {
            alleBarnMåværeUnder18år.hvisIkkeOppfylt {
                prototypeSøknad.boolsk(7).utfylt()
            }
        }
        val generatorSubsumsjon67 = prototypeSøknad.generator(67) med deltre

        val generatorSubsumsjon1718 = prototypeSøknad.generator(1718) med "Besvarte valg".deltre {
            "alle må være besvarte".alle(
                prototypeSøknad.envalg(17).utfylt(),
                prototypeSøknad.envalg(18).utfylt(),
            )
        }

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
                ),
                "NAV-systemer vil svare automatisk på følgende fakta".alle(
                    prototypeSøknad.dato(8).utfylt(),
                    prototypeSøknad.dato(9).utfylt(),
                ),
                "dokumentasjon".alle(
                    prototypeSøknad.boolsk(5) er true hvisOppfylt {
                        prototypeSøknad.boolsk(16) dokumenteresAv prototypeSøknad.dokument(15)
                    }
                ),
                "Generator med valg".alle(
                    generatorSubsumsjon1718
                ),
                "Land".alle(
                    prototypeSøknad.land(19).utfylt()
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
            ),
            Seksjon(
                "navseksjon",
                Rolle.nav,
                prototypeSøknad.dato(8),
                prototypeSøknad.dato(9),
            ),
            Seksjon(
                "dokumentasjon",
                Rolle.søker,
                prototypeSøknad.dokument(15)
            ),
            Seksjon(
                "seksjon3",
                Rolle.søker,
                prototypeSøknad.generator(1718),
                prototypeSøknad.envalg(17),
                prototypeSøknad.flervalg(18),
            ),
            Seksjon(
                "saksbehandler godkjenning",
                Rolle.saksbehandler,
                prototypeSøknad.boolsk(16)
            ),
            Seksjon(
                "Gyldige land",
                Rolle.søker,
                prototypeSøknad.land(19)
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

    private fun assertFerdig(søkerJson: ObjectNode) {
        assertTrue(søkerJson["ferdig"].asBoolean(), "Forventer at søknadsprosessen er ferdig og venter på godkjenning")
    }

    private fun assertIkkeFerdig(søkerJson: ObjectNode) {
        assertThrows<AssertionError> { assertFerdig(søkerJson) }
    }

    private fun assertBesvartGeneratorFaktum(
        forventetAntall: Int,
        generatorFaktumNavn: String,
        seksjon: String,
        søkerJson: ObjectNode
    ) {
        assertEquals(
            forventetAntall,
            søkerJson["seksjoner"].find { it["beskrivendeId"].asText() == seksjon }?.get("fakta")
                ?.find { it["beskrivendeId"].asText() == generatorFaktumNavn }?.get("svar")?.flatten()?.filter {
                    it.has("svar")
                }?.size
        )
    }

    private fun assertUbesvartGeneratorFaktum(
        generatorFaktumNavn: String,
        seksjon: String,
        søkerJson: ObjectNode,
        forventetAntall: Int = 1
    ) {
        assertEquals(
            forventetAntall,
            søkerJson["seksjoner"].find { it["beskrivendeId"].asText() == seksjon }?.get("fakta")
                ?.find { it["beskrivendeId"].asText() == generatorFaktumNavn }?.get("svar")?.flatten()?.filterNot {
                    it.has("svar")
                }?.size
        )
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
        assertEquals("søker_oppgave", søkerJson["@event_name"].asText())
        Assertions.assertDoesNotThrow { søkerJson["@id"].asText().also { UUID.fromString(it) } }
        Assertions.assertDoesNotThrow { søkerJson["@opprettet"].asText().also { LocalDateTime.parse(it) } }
        Assertions.assertDoesNotThrow { søkerJson["søknad_uuid"].asText().also { UUID.fromString(it) } }
        assertEquals("12020052345", søkerJson["fødselsnummer"].asText())
    }
}
