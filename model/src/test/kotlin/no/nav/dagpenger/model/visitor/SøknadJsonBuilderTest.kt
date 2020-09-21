package no.nav.dagpenger.model.visitor

import no.nav.dagpenger.model.fakta.FaktumNavn
import no.nav.dagpenger.model.fakta.faktum
import no.nav.dagpenger.model.søknad.Seksjon
import no.nav.dagpenger.model.søknad.Søknad
import org.junit.jupiter.api.Test
import kotlin.test.assertNotNull

class SøknadJsonBuilderTest {

    @Test
    fun `Lage søknad med uuid`() {

        val søknad = Søknad()

        val jsonBuilder = SøknadJsonBuilder(søknad)
        val json = jsonBuilder.resultat()

        println(jsonBuilder)
        assertNotNull(json["uuid"])
    }

    @Test
    fun `Lage søknad med seksjoner`(){

        val faktum = FaktumNavn(1, "navn").faktum<Boolean>()
        val seksjon = Seksjon(faktum)
        val søknad = Søknad(seksjon)

        val jsonBuilder = SøknadJsonBuilder(søknad)
        val json = jsonBuilder.resultat()

        println(jsonBuilder)

    }
}
