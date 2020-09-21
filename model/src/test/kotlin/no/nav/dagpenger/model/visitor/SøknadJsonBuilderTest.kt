package no.nav.dagpenger.model.visitor

import no.nav.dagpenger.model.fakta.FaktumNavn
import no.nav.dagpenger.model.fakta.faktum
import no.nav.dagpenger.model.søknad.Seksjon
import no.nav.dagpenger.model.søknad.Søknad
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class SøknadJsonBuilderTest {

    @Test
    fun `Lage søknad med uuid`() {

        val søknad = Søknad()

        val jsonBuilder = SøknadJsonBuilder(søknad)
        val json = jsonBuilder.resultat()

        println(jsonBuilder)
        assertNotNull(json["root"]["uuid"])
    }

    @Test
    fun `Lage søknad med seksjoner`() {

        val faktum = FaktumNavn(1, "navn").faktum<Boolean>()
        val faktum2 = FaktumNavn(2, "navn2").faktum<Boolean>()
        val seksjon = Seksjon(faktum)
        val seksjon2 = Seksjon(faktum, faktum2)
        val søknad = Søknad(seksjon, seksjon2)

        val jsonBuilder = SøknadJsonBuilder(søknad)
        val json = jsonBuilder.resultat()

        println(jsonBuilder)

        assertEquals(2, json["root"]["seksjoner"].size())
        assertEquals(2, json["fakta"].size())
        assertEquals(listOf(1, 2), json["root"]["seksjoner"][1]["fakta"].map { it.asInt() })
    }
}
