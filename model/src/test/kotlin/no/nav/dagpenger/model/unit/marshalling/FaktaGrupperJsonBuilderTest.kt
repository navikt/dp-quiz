package no.nav.dagpenger.model.unit.marshalling

import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.ja
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.marshalling.SøknadprosessJsonBuilder
import no.nav.dagpenger.model.seksjon.Seksjon
import no.nav.dagpenger.model.seksjon.Søknadprosess
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class FaktaGrupperJsonBuilderTest {

    @Test
    fun `Lage søknadprosess med uuid`() {

        val søknadprosess = Søknadprosess()

        val jsonBuilder = SøknadprosessJsonBuilder(søknadprosess)
        val json = jsonBuilder.resultat()

        assertNotNull(json["root"]["uuid"])
    }

    @Test
    fun `Lage søknadprosess med seksjoner`() {
        val søknad = Søknad(
            ja nei "navn1" id 1,
            ja nei "navn2" id 2
        )

        val seksjon = Seksjon("seksjon", Rolle.søker, søknad ja 1)
        val seksjon2 = Seksjon("seksjon", Rolle.søker, søknad ja 1, søknad ja 2)
        val søknadprosess = Søknadprosess(seksjon, seksjon2)

        val jsonBuilder = SøknadprosessJsonBuilder(søknadprosess)
        val json = jsonBuilder.resultat()

        assertEquals(2, json["root"]["seksjoner"].size())
        assertEquals(Rolle.søker.typeNavn, json["root"]["seksjoner"][0]["rolle"].asText())
        assertEquals(2, json["fakta"].size())
        assertEquals(listOf(1, 2), json["root"]["seksjoner"][1]["fakta"].map { it.asInt() })
    }
}
