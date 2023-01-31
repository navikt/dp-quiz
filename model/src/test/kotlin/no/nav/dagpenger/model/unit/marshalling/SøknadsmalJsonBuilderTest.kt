package no.nav.dagpenger.model.unit.marshalling

import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.boolsk
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.dato
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.desimaltall
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.dokument
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.envalg
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.flervalg
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.heltall
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.inntekt
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.land
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.periode
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.tekst
import no.nav.dagpenger.model.faktum.Fakta
import no.nav.dagpenger.model.faktum.Land
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.helpers.assertFaktaAsJson
import no.nav.dagpenger.model.helpers.assertGeneratorFaktaAsJson
import no.nav.dagpenger.model.helpers.assertValgFaktaAsJson
import no.nav.dagpenger.model.helpers.testPerson
import no.nav.dagpenger.model.helpers.testversjon
import no.nav.dagpenger.model.marshalling.SøknadsmalJsonBuilder
import no.nav.dagpenger.model.regel.er
import no.nav.dagpenger.model.seksjon.Seksjon
import no.nav.dagpenger.model.seksjon.Utredningsprosess
import no.nav.dagpenger.model.seksjon.Versjon
import no.nav.dagpenger.model.subsumsjon.Subsumsjon
import no.nav.dagpenger.model.subsumsjon.alle
import no.nav.dagpenger.model.subsumsjon.deltre
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class SøknadsmalJsonBuilderTest {
    private lateinit var prototypeFakta: Fakta

    @BeforeEach
    fun setup() {
        prototypeFakta = Fakta(
            testversjon,
            boolsk faktum "boolsk1" id 1,
            heltall faktum "heltall2" id 2,
            heltall faktum "heltall3" id 3,
            dato faktum "dato4" id 4,
            heltall faktum "generator5" id 5 genererer 3 og 4,
            desimaltall faktum "desimaltall6" id 6,
            dokument faktum "dokument7" id 7,
            inntekt faktum "inntekt8" id 8,
            heltall faktum "dato9" id 9 genererer 7 og 8,
            flervalg faktum "flervalg10" med "valg1" med "valg2" med "valg3" id 10,
            envalg faktum "envalg11" med "valg1" med "valg2" id 11,
            dato faktum "dato12" id 12,
            inntekt faktum "inntekt13" id 13,
            envalg faktum "envalg18" id 18 med "valg1" med "valg2",
            boolsk faktum "boolsk19" id 19,
            heltall faktum "generator14" id 14 genererer 12 og 13 og 18 og 19,
            tekst faktum "tekst15" id 15,
            periode faktum "periode16" id 16,
            periode faktum "pågåendePeriode17" id 17,
            land faktum "f20"
                gruppe "eøs" med listOf(Land("SWE"))
                gruppe "storbritannia" med listOf(Land("GBR"))
                gruppe "norge-jan-mayen" med listOf(Land("NOR")) id 20
        )
    }

    private fun søknadprosess(prototypeSubsumsjon: Subsumsjon): Utredningsprosess {
        val prototypeUtredningsprosess = Utredningsprosess(
            prototypeFakta,
            Seksjon(
                "seksjon1",
                Rolle.søker,
                prototypeFakta.boolsk(1),
                prototypeFakta.heltall(2),
                prototypeFakta.tekst(15),
                prototypeFakta.periode(16),
                prototypeFakta.periode(17)
            ),
            Seksjon(
                "seksjon2",
                Rolle.søker,
                prototypeFakta.heltall(3),
                prototypeFakta.dato(4),
                prototypeFakta.generator(5),
                prototypeFakta.desimaltall(6),
            ),
            Seksjon(
                "seksjon3",
                Rolle.søker,
                prototypeFakta.flervalg(10),
                prototypeFakta.envalg(11),
                prototypeFakta.dato(12),
                prototypeFakta.inntekt(13),
                prototypeFakta.envalg(18),
                prototypeFakta.boolsk(19),
                prototypeFakta.generator(14),
            ),
            Seksjon(
                "nav", Rolle.nav,
                prototypeFakta.dokument(7),
                prototypeFakta.inntekt(8),
                prototypeFakta.dato(9)
            ),
            Seksjon("seksjon4", Rolle.søker, prototypeFakta.land(20), prototypeFakta.inntekt(8)),
            rootSubsumsjon = prototypeSubsumsjon
        )

        return Versjon.Bygger(
            prototypeFakta,
            prototypeSubsumsjon,
            prototypeUtredningsprosess
        ).utredningsprosess(testPerson)
    }

    private fun søkerSubsumsjon() = "regel" deltre {
        "alle".alle(
            prototypeFakta.boolsk(1) er true
        )
    }

    @Test
    fun `serialisering av ubesvarte fakta til json`() {
        val regel = søkerSubsumsjon()
        val søknadprosess = søknadprosess(regel)
        val malJson = SøknadsmalJsonBuilder(søknadprosess).resultat()
        assertEquals(0, malJson["versjon_id"].asInt())
        assertEquals("test", malJson["versjon_navn"].asText())
        // NAV seksjoner skal ikke med
        assertEquals(4, malJson["seksjoner"].size())

        with(malJson["seksjoner"][0]) {
            assertEquals("seksjon1", this["beskrivendeId"].asText())
            assertEquals(5, this["fakta"].size())
            this["fakta"][0].assertValgFaktaAsJson(
                "1",
                "boolean",
                "boolsk1",
                listOf("søker"),
                listOf("svar.ja", "svar.nei")
            )
            this["fakta"][1].assertFaktaAsJson("2", "int", "heltall2", listOf("søker"))
            this["fakta"][2].assertFaktaAsJson("15", "tekst", "tekst15", listOf("søker"))
            this["fakta"][3].assertFaktaAsJson("16", "periode", "periode16", listOf("søker"))
            this["fakta"][4].assertFaktaAsJson("17", "periode", "pågåendePeriode17", listOf("søker"))
        }

        with(malJson["seksjoner"][1]) {
            assertEquals("seksjon2", this["beskrivendeId"].asText())
            assertEquals(2, this["fakta"].size())
            this["fakta"][0].assertGeneratorFaktaAsJson(
                "5", "generator", "generator5", listOf("søker"),
                assertTemplates = listOf(
                    { it.assertFaktaAsJson("3", "int", "heltall3", listOf("søker")) },
                    { it.assertFaktaAsJson("4", "localdate", "dato4", listOf("søker")) }
                )
            )
            this["fakta"][1].assertFaktaAsJson("6", "double", "desimaltall6", listOf("søker"))
        }
        with(malJson["seksjoner"][2]) {
            assertEquals("seksjon3", this["beskrivendeId"].asText())
            assertEquals(3, this["fakta"].size())
            this["fakta"][0].assertValgFaktaAsJson(
                "10",
                "flervalg",
                "flervalg10",
                listOf("søker"),
                listOf("valg1", "valg2", "valg3")
            )
            this["fakta"][1].assertValgFaktaAsJson(
                "11",
                "envalg",
                "envalg11",
                listOf("søker"),
                listOf("valg1", "valg2", "valg3")
            )
            this["fakta"][2].assertGeneratorFaktaAsJson(
                "14", "generator", "generator14", listOf("søker"),
                assertTemplates = listOf(
                    { it.assertFaktaAsJson("12", "localdate", "dato12", listOf("søker")) },
                    { it.assertFaktaAsJson("13", "inntekt", "inntekt13", listOf("søker")) },
                    {
                        it.assertValgFaktaAsJson(
                            "18",
                            "envalg",
                            "envalg18",
                            listOf("søker"),
                            listOf("valg1", "valg2")
                        )
                    },
                    {
                        it.assertValgFaktaAsJson(
                            "19",
                            "boolean",
                            "boolsk19",
                            listOf("søker"),
                            listOf("svar.ja", "svar.nei")
                        )
                    },
                )
            )
        }
        with(malJson["seksjoner"][3]) {
            assertEquals("seksjon4", this["beskrivendeId"].asText())
            assertEquals(2, this["fakta"].size())
            this["fakta"][0].assertFaktaAsJson("20", "land", "f20", listOf("søker"))
            assertEquals(3, this["fakta"][0]["grupper"].size())
            assertEquals(249, this["fakta"][0]["gyldigeLand"].size())
            // Kommer via avhengigAv
            this["fakta"][1].assertGeneratorFaktaAsJson(
                "9", "generator", "dato9", listOf("nav"),
                assertTemplates = listOf(
                    { it.assertFaktaAsJson("7", "dokument", "dokument7", listOf("nav")) },
                    { it.assertFaktaAsJson("8", "inntekt", "inntekt8", listOf("nav", "søker")) }
                )
            )
        }
    }
}
