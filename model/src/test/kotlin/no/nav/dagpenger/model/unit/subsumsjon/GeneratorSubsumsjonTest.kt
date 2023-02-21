package no.nav.dagpenger.model.unit.subsumsjon

import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.heltall
import no.nav.dagpenger.model.faktum.Fakta
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.helpers.TestProsesser
import no.nav.dagpenger.model.helpers.testversjon
import no.nav.dagpenger.model.regel.er
import no.nav.dagpenger.model.regel.med
import no.nav.dagpenger.model.regel.under
import no.nav.dagpenger.model.seksjon.Prosess
import no.nav.dagpenger.model.seksjon.Seksjon
import no.nav.dagpenger.model.subsumsjon.AlleSubsumsjon
import no.nav.dagpenger.model.subsumsjon.DeltreSubsumsjon
import no.nav.dagpenger.model.subsumsjon.deltre
import no.nav.dagpenger.model.subsumsjon.hvisOppfylt
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class GeneratorSubsumsjonTest {
    private lateinit var fakta: Fakta

    @BeforeEach
    fun setup() {
        fakta = Fakta(
            testversjon,
            heltall faktum "alder" id 1,
            heltall faktum "barn" id 2 genererer 1,
        )
    }

    @Test
    fun `Deltre template kan ikke ha oppfylte eller ikke oppfylte stier`() {
        val template = fakta heltall 1
        val deltre = "deltre template".deltre { template er 4 } hvisOppfylt { template er 5 }

        assertThrows<IllegalArgumentException> { fakta generator 2 med (deltre as DeltreSubsumsjon) }
    }

    @Test
    fun `Deltre template subsumsjon works`() {
        val alleBarnMåværeUnder18år = fakta heltall 1 under 18
        val deltre = "§ 1.2 har kun ikke myndige barn".deltre {
            alleBarnMåværeUnder18år
        }
        val subsumsjon = fakta generator 2 med deltre
        val prosess = Prosess(
            TestProsesser.Test,
            fakta,
            Seksjon("seksjon", Rolle.søker, fakta generator 2, fakta boolsk 1),
            rootSubsumsjon = subsumsjon,
        )

        prosess.generator(2).besvar(3)
        assertEquals(null, subsumsjon.resultat())
        assertEquals(3, (subsumsjon[0].oppfylt as AlleSubsumsjon).size)

        prosess.generator(2).besvar(2)
        assertEquals(null, subsumsjon.resultat())
        assertEquals(2, (subsumsjon[0].oppfylt as AlleSubsumsjon).size)

        prosess.heltall("1.1").besvar(8)
        prosess.heltall("1.2").besvar(8)
        assertEquals(true, subsumsjon.resultat())

        prosess.heltall("1.2").besvar(19)
        assertEquals(false, subsumsjon.resultat())
    }
}
