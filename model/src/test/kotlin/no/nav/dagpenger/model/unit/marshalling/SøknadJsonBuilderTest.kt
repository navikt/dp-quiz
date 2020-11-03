package no.nav.dagpenger.model.unit.marshalling

import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.helpers.NyttEksempel
import no.nav.dagpenger.model.marshalling.SøknadJsonBuilder
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

internal class SøknadJsonBuilderTest {
    private lateinit var søknad: Søknad

    @BeforeEach
    fun setup() {
        søknad = NyttEksempel().faktagrupper.søknad
    }

    @Test
    fun `bygger json`() {
        val json = SøknadJsonBuilder(søknad).resultat()

        assertEquals(25, json["fakta"]["faktum"].size())
        assertEquals(Rolle.nav.name, json["fakta"]["faktum"][0]["roller"][0].asText())
        assertEquals("1", json["fakta"]["versjonId"].asText())
        assertEquals("12345678901", json["fakta"]["fnr"].asText())
        assertNotNull(json["fakta"]["uuid"])
    }
}
