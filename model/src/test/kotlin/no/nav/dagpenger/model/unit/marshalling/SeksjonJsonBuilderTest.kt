package no.nav.dagpenger.model.unit.marshalling

import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.inntekt
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.ja
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.helpers.testSøknadprosess
import no.nav.dagpenger.model.marshalling.SeksjonJsonBuilder
import no.nav.dagpenger.model.seksjon.Seksjon
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class SeksjonJsonBuilderTest {
    @Test
    fun `Hente ut enkelt seksjon`() {
        val seksjon = Søknad(
            179,
            ja nei "navn" id 1
        ).testSøknadprosess().let {
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
        val søknad = Søknad(
            178,
            ja nei "verneplikt" id 1,
            inntekt faktum "inntekt" id 2 avhengerAv 1
        )

        val vernepliktFaktum = søknad ja 1
        val vernepliktSeksjon = Seksjon("Verneplikt", Rolle.nav, vernepliktFaktum)
        SeksjonJsonBuilder(vernepliktSeksjon).resultat().also {
            assertEquals(1, it["fakta"].size())
            assertEquals(listOf(1), it["root"]["fakta"].map { it.asInt() })
        }

        vernepliktFaktum.besvar(true)

        val avhengigSeksjon = Seksjon("Inntekt", Rolle.nav, søknad id 2)

        SeksjonJsonBuilder(avhengigSeksjon).resultat().also {
            assertEquals(2, it["fakta"].size())
            assertEquals(listOf(2, 1), it["root"]["fakta"].map { it.asInt() })
        }
    }
}
