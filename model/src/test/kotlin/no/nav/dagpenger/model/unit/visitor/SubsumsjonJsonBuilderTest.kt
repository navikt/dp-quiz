package no.nav.dagpenger.model.unit.visitor

import no.nav.dagpenger.model.fakta.FaktumNavn
import no.nav.dagpenger.model.fakta.Rolle
import no.nav.dagpenger.model.fakta.faktum
import no.nav.dagpenger.model.helpers.bursdag67
import no.nav.dagpenger.model.helpers.februar
import no.nav.dagpenger.model.helpers.januar
import no.nav.dagpenger.model.helpers.subsumsjonRoot
import no.nav.dagpenger.model.helpers.søknadsdato
import no.nav.dagpenger.model.helpers.virkningstidspunkt
import no.nav.dagpenger.model.helpers.ønsketdato
import no.nav.dagpenger.model.regel.etter
import no.nav.dagpenger.model.regel.har
import no.nav.dagpenger.model.søknad.Seksjon
import no.nav.dagpenger.model.visitor.SubsumsjonJsonBuilder
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class SubsumsjonJsonBuilderTest {

    @Test
    fun `Lage en subsumsjon med fakta`() {
        val faktumNavnId = 1
        val faktum = FaktumNavn<Boolean>(faktumNavnId, "faktum").faktum()
        val seksjon = Seksjon("seksjon", Rolle.søker, faktum)

        var jsonBuilder = SubsumsjonJsonBuilder(har(faktum))
        var jsonfakta = jsonBuilder.resultat()

        assertFalse(jsonfakta["root"]["navn"].isNull)
        assertFalse(jsonfakta["fakta"].isNull)
        assertTrue(jsonfakta["root"]["fakta"].isArray)
        assertEquals(1, jsonfakta["root"]["fakta"].size())
        assertEquals(listOf(faktumNavnId), jsonfakta["root"]["fakta"].map { it.asInt() })
        assertEquals("søker", jsonfakta["fakta"][0]["roller"][0].asText())
        assertEquals("boolean", jsonfakta["fakta"][0]["clazz"].asText())
        assertEquals(null, jsonfakta["fakta"][0]["svar"])

        faktum.besvar(true)
        jsonBuilder = SubsumsjonJsonBuilder((har(faktum)))
        jsonfakta = jsonBuilder.resultat()
        assertEquals(true, jsonfakta["fakta"][0]["svar"].asBoolean())
    }

    @Test
    fun `Finner avhengige fakta i json`() {
        val faktumNavnId = 1
        val faktum = FaktumNavn<Boolean>(faktumNavnId, "faktum").faktum()
        val avhengigFaktum = FaktumNavn<Boolean>(2, "faktumto").faktum()

        avhengigFaktum avhengerAv faktum

        val jsonBuilder = SubsumsjonJsonBuilder(har(faktum))
        val jsonfakta = jsonBuilder.resultat()

        assertEquals(listOf(2), jsonfakta["fakta"][0]["avhengigFakta"].map { it.asInt() })
    }

    @Test
    fun `Finner utledet fakta i json`() {
        subsumsjonRoot()
        val jsonBuilder = SubsumsjonJsonBuilder(virkningstidspunkt etter bursdag67)
        val json = jsonBuilder.resultat()

        assertEquals(3, json["fakta"][0]["fakta"].size())
        assertEquals(listOf(3, 2, 4), json["fakta"][0]["fakta"].map { it.asInt() })
    }

    @Test
    fun `Komplekse subsumsjoner i json`() {
        val comp = subsumsjonRoot()
        bursdag67.besvar(31.januar)
        søknadsdato.besvar(1.januar)
        ønsketdato.besvar(1.februar)
        val json = SubsumsjonJsonBuilder(comp).resultat()

        assertEquals(10, json["fakta"].size())
        assertEquals(2, json["root"]["subsumsjoner"].size())
        assertEquals(3, json["root"]["gyldig"]["subsumsjoner"].size())
        assertTrue(json["root"]["subsumsjoner"][0]["subsumsjoner"][0]["resultat"].asBoolean())
        assertFalse(json["root"]["subsumsjoner"][0]["subsumsjoner"][1]["resultat"].asBoolean())
        assertTrue(json["root"]["subsumsjoner"][0]["subsumsjoner"][2]["resultat"].isNull())
        assertEquals("localdate", json["fakta"][0]["clazz"].asText())
        assertEquals("localdate", json["fakta"][9]["clazz"].asText())
        assertEquals(listOf(5, 9), json["root"]["gyldig"]["subsumsjoner"][0]["fakta"].map { it.asInt() })
    }
}
