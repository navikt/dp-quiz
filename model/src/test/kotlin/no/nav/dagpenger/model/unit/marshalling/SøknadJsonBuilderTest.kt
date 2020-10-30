package no.nav.dagpenger.model.unit.marshalling

import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.ja
import no.nav.dagpenger.model.fakta.Fakta
import no.nav.dagpenger.model.fakta.Rolle
import no.nav.dagpenger.model.marshalling.SøknadJsonBuilder
import no.nav.dagpenger.model.søknad.Faktagrupper
import no.nav.dagpenger.model.søknad.Seksjon
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class SøknadJsonBuilderTest {

    @Test
    fun `Lage søknad med uuid`() {

        val søknad = Faktagrupper()

        val jsonBuilder = SøknadJsonBuilder(søknad)
        val json = jsonBuilder.resultat()

        assertNotNull(json["root"]["uuid"])
    }

    @Test
    fun `Lage søknad med seksjoner`() {
        val fakta = Fakta(
            ja nei "navn1" id 1,
            ja nei "navn2" id 2
        )

        val seksjon = Seksjon("seksjon", Rolle.søker, fakta ja 1)
        val seksjon2 = Seksjon("seksjon", Rolle.søker, fakta ja 1, fakta ja 2)
        val søknad = Faktagrupper(seksjon, seksjon2)

        val jsonBuilder = SøknadJsonBuilder(søknad)
        val json = jsonBuilder.resultat()

        assertEquals(2, json["root"]["seksjoner"].size())
        assertEquals(Rolle.søker.name, json["root"]["seksjoner"][0]["rolle"].asText())
        assertEquals(2, json["fakta"].size())
        assertEquals(listOf(1, 2), json["root"]["seksjoner"][1]["fakta"].map { it.asInt() })
    }
}
