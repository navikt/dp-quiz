package no.nav.dagpenger.model.unit.visitor

import no.nav.dagpenger.model.fakta.FaktumNavn
import no.nav.dagpenger.model.fakta.Rolle
import no.nav.dagpenger.model.fakta.faktum
import no.nav.dagpenger.model.søknad.Seksjon
import no.nav.dagpenger.model.søknad.Søknad
import no.nav.dagpenger.model.visitor.SøknadJsonBuilder
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class SøknadJsonBuilderTest {

    @Test
    fun `Lage søknad med uuid`() {

        val søknad = Søknad()

        val jsonBuilder = SøknadJsonBuilder(søknad)
        val json = jsonBuilder.resultat()

        assertNotNull(json["root"]["uuid"])
    }

    @Test
    fun `Lage søknad med seksjoner`() {

        val faktum = FaktumNavn<Boolean>(1, "navn").faktum()
        val faktum2 = FaktumNavn<Boolean>(2, "navn2").faktum()
        val seksjon = Seksjon("seksjon", Rolle.søker, faktum)
        val seksjon2 = Seksjon("seksjon", Rolle.søker, faktum, faktum2)
        val søknad = Søknad(seksjon, seksjon2)

        val jsonBuilder = SøknadJsonBuilder(søknad)
        val json = jsonBuilder.resultat()

        assertEquals(2, json["root"]["seksjoner"].size())
        assertEquals(Rolle.søker.name, json["root"]["seksjoner"][0]["rolle"].asText())
        assertEquals(2, json["fakta"].size())
        assertEquals(listOf(1, 2), json["root"]["seksjoner"][1]["fakta"].map { it.asInt() })
    }
}
