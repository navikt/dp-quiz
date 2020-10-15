package no.nav.dagpenger.model.unit.fakta

import no.nav.dagpenger.model.fakta.FaktumFactory.Companion.boolean
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

internal class FaktumFactoryTest {

    @Test
    fun boolean() {
        (boolean faktum "boolean" id 3).also { factory ->
            assertNotNull(factory)
            assertNotNull(factory.faktum)
            assertNotNull(factory.template)
        }
    }
}
