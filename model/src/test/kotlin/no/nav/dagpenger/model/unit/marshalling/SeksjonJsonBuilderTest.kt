package no.nav.dagpenger.model.unit.marshalling

import no.nav.dagpenger.model.factory.BaseFaktumFactory
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.ja
import no.nav.dagpenger.model.faktagrupper.Seksjon
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.helpers.testSøknad
import no.nav.dagpenger.model.marshalling.SeksjonJsonBuilder
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class SeksjonJsonBuilderTest {
    @Test
    fun `Hente ut enkelt seksjon`() {
        val seksjon = Søknad(
            ja nei "navn" id 1
        ).testSøknad().let {
            it[0]
        }

        val jsonBuilder = SeksjonJsonBuilder(seksjon)
        val json = jsonBuilder.resultat()

        assertEquals(1, json["fakta"].size())
        assertEquals(listOf(1), json["root"]["fakta"].map { it.asInt() })
        assertEquals(listOf(1), json["fakta"].map { it["id"].asInt() })
    }

    @Test
    fun `bygger faktaavhengigheter for seksjon`() {
        val fakta = Søknad(
            BaseFaktumFactory.Companion.ja nei "navn" id 1,
            BaseFaktumFactory.Companion.ja nei "jo" id 2 avhengerAv 1
        )

        val avhengigFakta = fakta ja 2
        val avhengigSeksjon = Seksjon("Avhengig", Rolle.nav, avhengigFakta)

        val jsonBuilder = SeksjonJsonBuilder(avhengigSeksjon)
        val json = jsonBuilder.resultat()

        assertEquals(2, json["fakta"].size())
        assertEquals(listOf(2), json["root"]["fakta"].map { it.asInt() })
    }
}
