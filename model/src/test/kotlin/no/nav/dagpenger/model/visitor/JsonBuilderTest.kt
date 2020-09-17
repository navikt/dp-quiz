package no.nav.dagpenger.model.visitor

import no.nav.dagpenger.model.fakta.FaktumNavn
import no.nav.dagpenger.model.fakta.faktum
import no.nav.dagpenger.model.regel.har
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

internal class JsonBuilderTest {

    @Test
    fun `Finner faktum-id i json`() {
        val faktumNavnId = 1
        val faktum = FaktumNavn(faktumNavnId, "faktum").faktum<Boolean>()

        val jsonfakta = JsonBuilder(har(faktum)).resultat()
        println(jsonfakta)

        assertEquals(faktumNavnId, jsonfakta["id"].asInt())
    }

    @Test
    @Disabled
    fun `Finner avhengige fakta i json`() {
        val faktumNavnId = 1
        val faktum = FaktumNavn(faktumNavnId, "faktum").faktum<Boolean>()
        val avhengigFaktum = FaktumNavn(2, "faktumto").faktum<Boolean>()

        avhengigFaktum avhengerAv faktum

        val jsonfakta = JsonBuilder(har(faktum)).resultat()
        println(jsonfakta)

        assertNotNull(jsonfakta["avhengigeFakta"][0])
    }
}
