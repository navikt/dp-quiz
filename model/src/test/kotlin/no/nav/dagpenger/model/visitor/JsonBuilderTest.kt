package no.nav.dagpenger.model.visitor

import no.nav.dagpenger.model.fakta.FaktumNavn
import no.nav.dagpenger.model.fakta.faktum
import no.nav.dagpenger.model.regel.har
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class JsonBuilderTest {

    @Test
    fun `Henter faktum id fra json`() {
        val faktumNavnId = 1
        val faktum = FaktumNavn(faktumNavnId, "faktum").faktum<Boolean>()

        val jsonfakta = JsonBuilder(har(faktum)).resultat()
        println(jsonfakta)

        assertEquals(faktumNavnId, jsonfakta["id"].asInt())
    }
}
