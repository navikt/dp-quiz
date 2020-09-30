package no.nav.dagpenger.model.unit.fakta

import no.nav.dagpenger.model.fakta.Alder
import no.nav.dagpenger.model.helpers.februar
import no.nav.dagpenger.model.helpers.januar
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class AlderTest {

    @Test
    fun `alder på gitt dato`() {
        val alder = Alder("12020052345")
        assertEquals(17, alder.alderPåDato(1.januar))
        assertEquals(18, alder.alderPåDato(12.februar))
    }
}
