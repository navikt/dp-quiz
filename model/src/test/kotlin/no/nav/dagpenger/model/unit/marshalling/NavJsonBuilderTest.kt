package no.nav.dagpenger.model.unit.marshalling

import com.fasterxml.jackson.databind.JsonNode
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.dato
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.ja
import no.nav.dagpenger.model.factory.UtledetFaktumFactory.Companion.maks
import no.nav.dagpenger.model.factory.ValgFaktumFactory.Companion.valg
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.helpers.januar
import no.nav.dagpenger.model.helpers.testPerson
import no.nav.dagpenger.model.marshalling.FaktumNavBehov
import no.nav.dagpenger.model.marshalling.NavJsonBuilder
import no.nav.dagpenger.model.regel.er
import no.nav.dagpenger.model.seksjon.Seksjon
import no.nav.dagpenger.model.seksjon.Søknadprosess
import no.nav.dagpenger.model.seksjon.Versjon
import no.nav.dagpenger.model.subsumsjon.alle
import no.nav.dagpenger.model.subsumsjon.så
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class NavJsonBuilderTest {

    @Test
    fun `bygger behov event`() {
        val prototypeSøknad = Søknad(
            500,
            ja nei "f1" id 1,
            ja nei "f1" id 2 avhengerAv 1,
            ja nei "f3" id 3,
            ja nei "f4" id 4 avhengerAv 7 og 8,
            dato faktum "f5" id 5,
            dato faktum "f6" id 6,
            maks dato "f56" av 5 og 6 id 7,
            valg faktum "f8" ja "jaValg1" ja "jaValg2" nei "neiValg1" nei "neiValg2" id 8
        )

        val prototypeSubsumsjon =
            prototypeSøknad.ja(1) er true så
                "alle".alle(
                    prototypeSøknad.ja(2) er true,
                    prototypeSøknad.ja(3) er true,
                    prototypeSøknad.ja(4) er true,
                    prototypeSøknad.valg(8) er true
                )

        val søkerSeksjon = Seksjon("seksjon søker", Rolle.søker, prototypeSøknad.ja(1))
        val navSeksjon = Seksjon(
            "seksjon nav",
            Rolle.nav,
            prototypeSøknad.ja(2),
            prototypeSøknad.ja(3),
            prototypeSøknad.ja(4),
            prototypeSøknad.dato(7)
        )

        val prototypeFaktagrupper = Søknadprosess(
            prototypeSøknad,
            søkerSeksjon,
            navSeksjon,
            rootSubsumsjon = prototypeSubsumsjon
        )

        Versjon(
            prototypeSøknad,
            prototypeSubsumsjon,
            mapOf(Versjon.UserInterfaceType.Web to prototypeFaktagrupper)
        )

        val fakta = Versjon.id(Versjon.siste).søknadprosess(testPerson, Versjon.UserInterfaceType.Web)
        FaktumNavBehov(
            Versjon.versjoner.keys.maxOf { it },
            mapOf(
                1 to "f1Behov",
                2 to "f2Behov",
                3 to "f3Behov",
                4 to "f4Behov",
                5 to "f5Behov",
                6 to "f6Behov",
                7 to "f7Behov",
                8 to "f8Behov"
            )
        )

        fakta.ja(1).besvar(true)
        fakta.dato(5).besvar(1.januar)

        assertBehovJson(
            json = NavJsonBuilder(fakta, "seksjon nav").resultat(),
            faktumOgBehov = listOf(2 to "f2Behov", 3 to "f3Behov", 6 to "f6Behov"),
            avhengigeBehov = listOf("f1Behov")
        )

        fakta.dato(6).besvar(1.januar)
        fakta.ja(9).besvar(true)

        assertBehovJson(
            json = NavJsonBuilder(fakta, "seksjon nav").resultat(),
            faktumOgBehov = listOf(2 to "f2Behov", 3 to "f3Behov", 4 to "f4Behov"),
            avhengigeBehov = listOf("f7Behov", "f8Behov")
        )

        fakta.dato(2).besvar(1.januar)
        fakta.dato(4).besvar(1.januar)

        assertBehovJson(
            json = NavJsonBuilder(fakta, "seksjon nav").resultat(),
            faktumOgBehov = listOf(3 to "f3Behov"),
            avhengigeBehov = emptyList()
        )

        fakta.dato(3).besvar(1.januar)
    }

    private fun assertBehovJson(
        json: JsonNode,
        faktumOgBehov: List<Pair<Int, String>>,
        avhengigeBehov: List<String>
    ) {
        val faktumOgBehovMap = faktumOgBehov.toMap()
        assertEquals("faktum_svar", json["@event_name"].asText())
        assertEquals("folkeregisterident", json["identer"][0]["type"].asText())
        assertEquals("12020052345", json["identer"][0]["id"].asText())
        assertTrue(json.has("@id"))
        assertTrue(json.has("@opprettet"))
        assertEquals(faktumOgBehovMap.values.toList(), json["@behov"].map { it.asText() })
        assertEquals(faktumOgBehovMap.keys.toString(), json["fakta"].map { it["id"].asText() }.toString())
        avhengigeBehov.forEach { avhengigBehov ->
            assertTrue(json.has(avhengigBehov))
            assertNotNull(json[avhengigBehov])
        }
    }
}
