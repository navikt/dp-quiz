package no.nav.dagpenger.model.unit.visitor

import no.nav.dagpenger.model.fakta.FaktumNavn
import no.nav.dagpenger.model.fakta.Rolle
import no.nav.dagpenger.model.fakta.faktum
import no.nav.dagpenger.model.søknad.Seksjon
import no.nav.dagpenger.model.visitor.SeksjonJsonBuilder
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class SeksjonJsonBuilderTest {
    @Test
    fun `Hente ut enkelt seksjon`() {
        val faktum = FaktumNavn(1, "navn").faktum(Boolean::class.java)
        val seksjon = Seksjon("seksjon", Rolle.søker, faktum)

        val jsonBuilder = SeksjonJsonBuilder(seksjon)
        val json = jsonBuilder.resultat()

        assertEquals(1, json["fakta"].size())
        assertEquals(listOf(1), json["root"]["fakta"].map { it.asInt() })
        assertEquals(listOf(1), json["fakta"].map { it["id"].asInt() })
    }
}
