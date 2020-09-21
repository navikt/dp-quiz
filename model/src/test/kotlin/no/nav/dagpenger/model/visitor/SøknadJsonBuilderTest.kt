package no.nav.dagpenger.model.visitor

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
}
