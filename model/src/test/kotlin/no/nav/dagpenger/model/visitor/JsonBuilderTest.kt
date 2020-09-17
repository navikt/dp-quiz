package no.nav.dagpenger.model.visitor

import no.nav.dagpenger.model.fakta.FaktumNavn
import no.nav.dagpenger.model.fakta.faktum
import no.nav.dagpenger.model.regel.har
import org.junit.jupiter.api.Test

internal class JsonBuilderTest {

    @Test
    fun `a`() {
        val faktumNavnId = 1
        val faktum = FaktumNavn(faktumNavnId, "faktum").faktum<Boolean>()

        JsonBuilder(har(faktum)).resultat().also {
            println(it)
        }
    }
}
