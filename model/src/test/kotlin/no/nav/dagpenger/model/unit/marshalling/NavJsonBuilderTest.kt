package no.nav.dagpenger.model.unit.marshalling

import com.fasterxml.jackson.databind.JsonNode
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.dato
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.heltall
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.ja
import no.nav.dagpenger.model.factory.UtledetFaktumFactory.Companion.maks
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.helpers.desember
import no.nav.dagpenger.model.helpers.februar
import no.nav.dagpenger.model.helpers.januar
import no.nav.dagpenger.model.helpers.testPerson
import no.nav.dagpenger.model.marshalling.FaktumNavBehov
import no.nav.dagpenger.model.marshalling.NavJsonBuilder
import no.nav.dagpenger.model.regel.er
import no.nav.dagpenger.model.regel.har
import no.nav.dagpenger.model.regel.mellom
import no.nav.dagpenger.model.seksjon.Seksjon
import no.nav.dagpenger.model.seksjon.Søknadprosess
import no.nav.dagpenger.model.seksjon.Versjon
import no.nav.dagpenger.model.subsumsjon.alle
import no.nav.dagpenger.model.subsumsjon.makro
import no.nav.dagpenger.model.subsumsjon.så
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class NavJsonBuilderTest {

    @Test
    fun `bygger behov event`() {

        val prototypeSøknad = Søknad(
            0,
            ja nei "f1" id 1,
            ja nei "f1" id 2 avhengerAv 1,
            ja nei "f3" id 3,
            ja nei "f4" id 4 avhengerAv 7,
            dato faktum "f5" id 5,
            dato faktum "f6" id 6,
            maks dato "f56" av 5 og 6 id 7,
            heltall faktum "periode" id 8 genererer 9 og 10,
            dato faktum "fom" id 9,
            dato faktum "tom" id 10

        )

        val f1Faktum = prototypeSøknad.ja(1)
        val f2Faktum = prototypeSøknad.ja(2)
        val f3Faktum = prototypeSøknad.ja(3)
        val f4Faktum = prototypeSøknad.ja(4)
        val f7Faktum = prototypeSøknad.dato(5)
        val f8Faktum = prototypeSøknad.generator(8)
        val f9Faktum = prototypeSøknad.dato(9)
        val f10Faktum = prototypeSøknad.dato(10)

        val periodeSubsumsjon = f8Faktum har "periode".makro(
            f7Faktum mellom f9Faktum og f10Faktum
        )

        val prototypeSubsumsjon =
            f1Faktum er true så
                "alle".alle(
                    f2Faktum er true,
                    f3Faktum er true,
                    f4Faktum er true,
                    periodeSubsumsjon
                )

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
            f10Faktum
        )

        val prototypeFaktagrupper = Søknadprosess(
            prototypeSøknad,
            søkerSeksjon,
            navSeksjon,
            rootSubsumsjon = prototypeSubsumsjon
        )

        val faktumNavBehov = FaktumNavBehov(
            mapOf(
                1 to "f1Behov",
                2 to "f2Behov",
                3 to "f3Behov",
                4 to "f4Behov",
                5 to "f5Behov",
                6 to "f6Behov",
                7 to "f7Behov",
                8 to "f8Behov",
                9 to "f9Behov"
            )
        )

        val fakta = Versjon.Bygger(
            prototypeSøknad,
            prototypeSubsumsjon,
            mapOf(Versjon.UserInterfaceType.Web to prototypeFaktagrupper),
            faktumNavBehov
        ).registrer().søknadprosess(testPerson, Versjon.UserInterfaceType.Web)

        fakta.ja(1).besvar(true)
        fakta.dato(5).besvar(1.januar)

        assertBehovJson(
            json = NavJsonBuilder(fakta, "seksjon nav").resultat(),
            faktumOgBehov = mapOf(2 to "f2Behov", 3 to "f3Behov", 8 to "f8Behov", 6 to "f6Behov"),
            avhengigeBehov = listOf("f1Behov")
        )

        fakta.dato(6).besvar(1.januar)

        assertBehovJson(
            json = NavJsonBuilder(fakta, "seksjon nav").resultat(),
            faktumOgBehov = mapOf(2 to "f2Behov", 3 to "f3Behov", 4 to "f4Behov", 8 to "f8Behov"),
            avhengigeBehov = listOf("f7Behov")
        )

        fakta.dato(2).besvar(1.januar)
        fakta.dato(4).besvar(1.januar)

        assertBehovJson(
            json = NavJsonBuilder(fakta, "seksjon nav").resultat(),
            faktumOgBehov = mapOf(3 to "f3Behov", 8 to "f8Behov"),
            avhengigeBehov = emptyList()
        )

        fakta.dato(3).besvar(1.januar)

        NavJsonBuilder(fakta, "seksjon nav").resultat().also {
            assertBehovJson(
                json = it,
                faktumOgBehov = mapOf(8 to "f8Behov"),
                avhengigeBehov = emptyList()
            )
            assertEquals(
                """[{"id":"9","navn":"fom","clazz":"localdate"},{"id":"10","navn":"tom","clazz":"localdate"}]""",
                it["fakta"][0]["templates"].toString()
            )
        }

        fakta.generator(8).besvar(1)
        fakta.dato("9.1").besvar(31.desember(2017))
        fakta.dato("10.1").besvar(2.februar)

        assertBehovJson(
            json = NavJsonBuilder(fakta, "seksjon nav").resultat(),
            faktumOgBehov = mapOf(),
            avhengigeBehov = emptyList()
        )
    }

    private fun assertBehovJson(
        json: JsonNode,
        faktumOgBehov: Map<Int, String>,
        avhengigeBehov: List<String>
    ) {
        assertEquals("faktum_svar", json["@event_name"].asText())
        assertEquals("folkeregisterident", json["identer"][0]["type"].asText())
        assertEquals("12020052345", json["identer"][0]["id"].asText())
        assertTrue(json.has("@id"))
        assertTrue(json.has("@opprettet"))
        assertEquals(faktumOgBehov.values.toList(), json["@behov"].map { it.asText() })
        assertEquals(faktumOgBehov.keys.toString(), json["fakta"].map { it["id"].asText() }.toString())
        avhengigeBehov.forEach { avhengigBehov ->
            assertTrue(json.has(avhengigBehov))
            assertNotNull(json[avhengigBehov])
        }
    }
}
