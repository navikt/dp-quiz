package no.nav.dagpenger.model.visitor

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.ObjectWriter
import no.nav.dagpenger.model.fakta.FaktumNavn
import no.nav.dagpenger.model.fakta.faktum
import no.nav.dagpenger.model.helpers.bursdag67
import no.nav.dagpenger.model.helpers.subsumsjonRoot
import no.nav.dagpenger.model.helpers.virkningstidspunkt
import no.nav.dagpenger.model.regel.etter
import no.nav.dagpenger.model.regel.har
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class JsonBuilderTest {

    @Test
    fun `Lage en subsubsjon med fakta`() {
        val faktumNavnId = 1
        val faktum = FaktumNavn(faktumNavnId, "faktum").faktum<Boolean>()

        val jsonfakta = JsonBuilder(har(faktum)).resultat()
        println(jsonfakta)

        assertFalse(jsonfakta["navn"].isNull)
        assertTrue(jsonfakta["fakta"].isArray)
        assertEquals(1, jsonfakta["fakta"].size())
        assertEquals(faktumNavnId, jsonfakta["fakta"][0]["id"].asInt())
    }

    @Test
    fun `Finner avhengige fakta i json`() {
        val faktumNavnId = 1
        val faktum = FaktumNavn(faktumNavnId, "faktum").faktum<Boolean>()
        val avhengigFaktum = FaktumNavn(2, "faktumto").faktum<Boolean>()

        avhengigFaktum avhengerAv faktum

        val jsonfakta = JsonBuilder(har(faktum)).resultat()
        println(jsonfakta)

        assertEquals(listOf(2), jsonfakta["fakta"][0]["avhengigFakta"].map { it.asInt() })
    }

    @Test
    fun `Finner utledet fakta i json`() {
        subsumsjonRoot()
        val json = JsonBuilder(virkningstidspunkt etter bursdag67).resultat()

        val string = ObjectMapper().writerWithDefaultPrettyPrinter<ObjectWriter>().writeValueAsString(json)
        println(string)

        assertEquals(3, json["fakta"][0]["fakta"].size())
        assertEquals(4, json["fakta"][0]["fakta"][2]["id"].asInt())
    }
}
