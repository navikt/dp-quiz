package no.nav.dagpenger.model.unit.subsumsjon

import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.heltall
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.helpers.testversjon
import no.nav.dagpenger.model.regel.er
import no.nav.dagpenger.model.regel.med
import no.nav.dagpenger.model.regel.under
import no.nav.dagpenger.model.seksjon.Seksjon
import no.nav.dagpenger.model.seksjon.Søknadprosess
import no.nav.dagpenger.model.subsumsjon.AlleSubsumsjon
import no.nav.dagpenger.model.subsumsjon.DeltreSubsumsjon
import no.nav.dagpenger.model.subsumsjon.deltre
import no.nav.dagpenger.model.subsumsjon.hvisOppfylt
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class GeneratorSubsumsjonTest {
    private lateinit var søknad: Søknad

    @BeforeEach
    fun setup() {
        søknad = Søknad(
            testversjon,
            heltall faktum "alder" id 1,
            heltall faktum "barn" id 2 genererer 1
        )
    }

    @Test
    fun `Deltre template kan ikke ha oppfylte eller ikke oppfylte stier`() {
        val template = søknad heltall 1
        val deltre = "deltre template".deltre { template er 4 } hvisOppfylt { template er 5 }

        assertThrows<IllegalArgumentException> { søknad generator 2 med (deltre as DeltreSubsumsjon) }
    }

    @Test
    fun `Deltre template subsumsjon works`() {
        val alleBarnMåværeUnder18år = søknad heltall 1 under 18
        val deltre = "§ 1.2 har kun ikke myndige barn".deltre {
            alleBarnMåværeUnder18år
        }
        val subsumsjon = søknad generator 2 med deltre
        val søknadprosess = Søknadprosess(
            søknad,
            Seksjon("seksjon", Rolle.søker, søknad generator 2, søknad boolsk 1),
            rootSubsumsjon = subsumsjon
        )

        søknadprosess.generator(2).besvar(3)
        assertEquals(null, subsumsjon.resultat())
        assertEquals(3, (subsumsjon[0].oppfylt as AlleSubsumsjon).size)

        søknadprosess.generator(2).besvar(2)
        assertEquals(null, subsumsjon.resultat())
        assertEquals(2, (subsumsjon[0].oppfylt as AlleSubsumsjon).size)

        søknadprosess.heltall("1.1").besvar(8)
        søknadprosess.heltall("1.2").besvar(8)
        assertEquals(true, subsumsjon.resultat())

        søknadprosess.heltall("1.2").besvar(19)
        assertEquals(false, subsumsjon.resultat())
    }
}
