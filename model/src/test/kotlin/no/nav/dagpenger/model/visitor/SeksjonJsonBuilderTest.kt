package no.nav.dagpenger.model.visitor

import no.nav.dagpenger.model.fakta.FaktumNavn
import no.nav.dagpenger.model.fakta.Rolle
import no.nav.dagpenger.model.fakta.faktum
import no.nav.dagpenger.model.søknad.Seksjon
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class SeksjonJsonBuilderTest {
    @Test
    fun `Hente ut enkelt seksjon`() {
        val faktum = FaktumNavn(1, "navn").faktum(Boolean::class.java)
        val seksjon = Seksjon(Rolle.søker, faktum)

        val jsonBuilder = SeksjonJsonBuilder(seksjon)
        val json = jsonBuilder.resultat()

        println(jsonBuilder)

        assertEquals(1, json["fakta"].size())
        assertEquals(listOf(1), json["root"]["fakta"].map { it.asInt() })
        assertEquals(listOf(1), json["fakta"].map { it["id"].asInt() })
    }
}