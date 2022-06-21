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
import no.nav.dagpenger.model.faktum.Land
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.helpers.testPerson
import no.nav.dagpenger.model.helpers.testversjon
import no.nav.dagpenger.model.helpers.toPrettyJson
import no.nav.dagpenger.model.marshalling.SøknadsmalJsonBuilder
import no.nav.dagpenger.model.regel.er
import no.nav.dagpenger.model.seksjon.Seksjon
import no.nav.dagpenger.model.seksjon.Søknadprosess
import no.nav.dagpenger.model.seksjon.Versjon
import no.nav.dagpenger.model.subsumsjon.Subsumsjon
import no.nav.dagpenger.model.subsumsjon.alle
import no.nav.dagpenger.model.subsumsjon.deltre
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class SøknadsmalJsonBuilderTest {
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
                prototypeSøknad.envalg(18),
                prototypeSøknad.boolsk(19),
                prototypeSøknad.generator(14),
            ),
            Seksjon("seksjon4", Rolle.søker, prototypeSøknad.land(20)),
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

        val malJson = SøknadsmalJsonBuilder(søknadprosess).resultat()
        println(malJson.toPrettyJson())

        assertEquals(0, malJson["versjon_id"].asInt())
        assertEquals("test", malJson["versjon_navn"].asText())
        assertEquals(5, malJson["seksjoner"].size())

        val førsteSeksjon = malJson["seksjoner"][0]
        assertEquals("seksjon1", førsteSeksjon["beskrivendeId"].asText())
        assertEquals(5, førsteSeksjon["fakta"].size())
        førsteSeksjon["fakta"][0].assertValgFaktaAsJson(
            "1",
            "boolean",
            "boolsk1",
            listOf("søker"),
            listOf("svar.ja", "svar.nei")
        )

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
                { it.assertFaktaAsJson("13", "inntekt", "inntekt13", listOf("søker")) },
                { it.assertValgFaktaAsJson("18", "envalg", "envalg18", listOf("søker"), listOf("valg1", "valg2")) },
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
        val fjerdeSeksjon = malJson["seksjoner"][3]
        assertEquals("seksjon4", fjerdeSeksjon["beskrivendeId"].asText())
        assertEquals(1, fjerdeSeksjon["fakta"].size())
        fjerdeSeksjon["fakta"][0].assertFaktaAsJson("20", "land", "f20", listOf("søker"))
        assertEquals(3, fjerdeSeksjon["fakta"][0]["grupper"].size())
        assertEquals(249, fjerdeSeksjon["fakta"][0]["gyldigeLand"].size())

        val navSeksjon = malJson["seksjoner"][4]
        assertEquals("nav", navSeksjon["beskrivendeId"].asText())
        assertEquals(3, navSeksjon["fakta"].size())
        navSeksjon["fakta"][0].assertFaktaAsJson("7", "dokument", "dokument7", listOf("nav"))
        navSeksjon["fakta"][1].assertFaktaAsJson("8", "inntekt", "inntekt8", listOf("nav"))
        navSeksjon["fakta"][2].assertFaktaAsJson("9", "localdate", "dato9", listOf("nav"))
    }
}
