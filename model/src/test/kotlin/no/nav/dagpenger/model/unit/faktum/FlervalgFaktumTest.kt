package no.nav.dagpenger.model.unit.faktum

import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.flervalg
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.faktum.Valg
import no.nav.dagpenger.model.helpers.testSøknadprosess
import no.nav.dagpenger.model.helpers.testversjon
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows

internal class FlervalgFaktumTest {

    @Test
    fun `Støtter å ta i mot lovlige flervalgalternativer`() {
        val søknad = Søknad(testversjon, flervalg faktum "flervalg" id 1 med "valg1" med "valg2").testSøknadprosess()
        val flervalgFaktum = søknad.faktum<Valg>(1)

        assertThrows<IllegalArgumentException> { flervalgFaktum.besvar(Valg(listOf("bla"))) }
        assertDoesNotThrow { flervalgFaktum.besvar(Valg(listOf("valg1")))}
        assertDoesNotThrow { flervalgFaktum.besvar(Valg(listOf("valg2"))) }
    }
}
