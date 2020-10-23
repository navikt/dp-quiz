package no.nav.dagpenger.model.unit.marshalling

import no.nav.dagpenger.model.fakta.Fakta
import no.nav.dagpenger.model.fakta.Rolle
import no.nav.dagpenger.model.helpers.NyEksempel
import no.nav.dagpenger.model.marshalling.FaktaJsonBuilder
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

internal class FaktaJsonBuilderTest {
    private lateinit var fakta: Fakta

    @BeforeEach
    fun setup() {
        fakta = NyEksempel().søknad.fakta
    }

    @Test
    fun `bygger json`() {
        val json = FaktaJsonBuilder(fakta).resultat()

        assertEquals(20, json["fakta"]["faktum"].size())
        assertEquals(Rolle.nav.name, json["fakta"]["faktum"][0]["roller"][0].asText())
        assertEquals("1", json["fakta"]["versjonId"].asText())
        assertEquals("12345678901", json["fakta"]["fnr"].asText())
        assertNotNull(json["fakta"]["uuid"])
    }
}
