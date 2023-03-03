package no.nav.dagpenger.model.unit.marshalling

import com.fasterxml.jackson.databind.JsonNode
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.boolsk
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.dato
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.heltall
import no.nav.dagpenger.model.factory.UtledetFaktumFactory.Companion.maks
import no.nav.dagpenger.model.faktum.Fakta
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.helpers.TestProsesser
import no.nav.dagpenger.model.helpers.desember
import no.nav.dagpenger.model.helpers.februar
import no.nav.dagpenger.model.helpers.januar
import no.nav.dagpenger.model.helpers.testBygger
import no.nav.dagpenger.model.helpers.testPerson
import no.nav.dagpenger.model.helpers.testversjon
import no.nav.dagpenger.model.marshalling.FaktumNavBehov
import no.nav.dagpenger.model.marshalling.NavJsonBuilder
import no.nav.dagpenger.model.regel.er
import no.nav.dagpenger.model.regel.har
import no.nav.dagpenger.model.regel.mellom
import no.nav.dagpenger.model.seksjon.Henvendelser
import no.nav.dagpenger.model.seksjon.Prosess
import no.nav.dagpenger.model.seksjon.Seksjon
import no.nav.dagpenger.model.subsumsjon.Subsumsjon
import no.nav.dagpenger.model.subsumsjon.alle
import no.nav.dagpenger.model.subsumsjon.deltre
import no.nav.dagpenger.model.subsumsjon.hvisOppfylt
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class NavJsonBuilderTest {
    @Test
    fun `bygger behov event`() {
        val prototypeFakta = Fakta(
            testversjon,
            boolsk faktum "f1" id 1,
            boolsk faktum "f1" id 2 avhengerAv 1,
            boolsk faktum "f3" id 3,
            boolsk faktum "f4" id 4 avhengerAv 7,
            dato faktum "f5" id 5,
            dato faktum "f6" id 6,
            maks dato "f56" av 5 og 6 id 7,
            heltall faktum "periode" id 8 genererer 9 og 10,
            dato faktum "fom" id 9,
            dato faktum "tom" id 10,
        ).also {
            Henvendelser.FaktaBygger(
                it,
                FaktumNavBehov(
                    mapOf(
                        1 to "f1Behov",
                        2 to "f2Behov",
                        3 to "f3Behov",
                        4 to "f4Behov",
                        5 to "f5Behov",
                        6 to "f6Behov",
                        7 to "f7Behov",
                        8 to "f8Behov",
                        9 to "f9Behov",
                    ),
                ),
            ).registrer()
        }
        val f1Faktum = prototypeFakta.boolsk(1)
        val f2Faktum = prototypeFakta.boolsk(2)
        val f3Faktum = prototypeFakta.boolsk(3)
        val f4Faktum = prototypeFakta.boolsk(4)
        val f7Faktum = prototypeFakta.dato(5)
        val f8Faktum = prototypeFakta.generator(8)
        val f9Faktum = prototypeFakta.dato(9)
        val f10Faktum = prototypeFakta.dato(10)
        val periodeSubsumsjon = f8Faktum har "periode".deltre {
            f7Faktum mellom f9Faktum og f10Faktum
        }
        val prototypeSubsumsjon: Subsumsjon =
            f1Faktum er true hvisOppfylt {
                "alle".alle(
                    f2Faktum er true,
                    f3Faktum er true,
                    f4Faktum er true,
                    periodeSubsumsjon,
                )
            }
        val søkerSeksjon = Seksjon("seksjon søker", Rolle.søker, f1Faktum)
        val navSeksjon = Seksjon(
            "seksjon nav",
            Rolle.nav,
            f1Faktum,
            f2Faktum,
            f3Faktum,
            f4Faktum,
            f7Faktum,
            f8Faktum,
            f9Faktum,
            f10Faktum,
        )
        val prototypeProsess = Prosess(
            TestProsesser.Test,
            prototypeFakta,
            søkerSeksjon,
            navSeksjon,
            rootSubsumsjon = prototypeSubsumsjon,
        )
        val prosess = prototypeProsess.testBygger(testPerson)

        prosess.boolsk(1).besvar(true)
        prosess.dato(5).besvar(1.januar)

        assertBehovJson(
            json = NavJsonBuilder(prosess, "seksjon nav").resultat(),
            faktumOgBehov = mapOf(2 to "f2Behov", 3 to "f3Behov", 8 to "f8Behov", 6 to "f6Behov"),
            avhengigeBehov = listOf("f1Behov"),
        )

        prosess.dato(6).besvar(1.januar)

        assertBehovJson(
            json = NavJsonBuilder(prosess, "seksjon nav").resultat(),
            faktumOgBehov = mapOf(2 to "f2Behov", 3 to "f3Behov", 4 to "f4Behov", 8 to "f8Behov"),
            avhengigeBehov = listOf("f7Behov"),
        )

        prosess.dato(2).besvar(1.januar)
        prosess.dato(4).besvar(1.januar)

        assertBehovJson(
            json = NavJsonBuilder(prosess, "seksjon nav").resultat(),
            faktumOgBehov = mapOf(3 to "f3Behov", 8 to "f8Behov"),
            avhengigeBehov = emptyList(),
        )

        prosess.dato(3).besvar(1.januar)

        NavJsonBuilder(prosess, "seksjon nav").resultat().also {
            assertBehovJson(
                json = it,
                faktumOgBehov = mapOf(8 to "f8Behov"),
                avhengigeBehov = emptyList(),
            )
            assertEquals(
                """[{"id":"9","navn":"fom","type":"localdate"},{"id":"10","navn":"tom","type":"localdate"}]""",
                it["fakta"][0]["templates"].toString(),
            )
        }

        prosess.generator(8).besvar(1)
        prosess.dato("9.1").besvar(31.desember(2017))
        prosess.dato("10.1").besvar(2.februar)

        assertBehovJson(
            json = NavJsonBuilder(prosess, "seksjon nav").resultat(),
            faktumOgBehov = mapOf(),
            avhengigeBehov = emptyList(),
        )
    }

    private fun assertBehovJson(
        json: JsonNode,
        faktumOgBehov: Map<Int, String>,
        avhengigeBehov: List<String>,
    ) {
        assertEquals("faktum_svar", json["@event_name"].asText())
        assertEquals("folkeregisterident", json["identer"][0]["type"].asText())
        assertEquals("12020052345", json["identer"][0]["id"].asText())
        assertTrue(json.has("@id"))
        assertTrue(json.has("@behovId"))
        assertTrue(json.has("@opprettet"))
        assertEquals(faktumOgBehov.values.toList(), json["@behov"].map { it.asText() })
        assertEquals(faktumOgBehov.keys.toString(), json["fakta"].map { it["id"].asText() }.toString())
        avhengigeBehov.forEach { avhengigBehov ->
            assertTrue(json.has(avhengigBehov))
            assertNotNull(json[avhengigBehov])
        }
    }
}
