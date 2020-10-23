package no.nav.dagpenger.model.unit.fakta

import no.nav.dagpenger.model.factory.BaseFaktumFactory
import no.nav.dagpenger.model.fakta.Fakta
import no.nav.dagpenger.model.fakta.Faktum
import no.nav.dagpenger.model.fakta.Rolle
import no.nav.dagpenger.model.helpers.søknad
import no.nav.dagpenger.model.regel.er
import no.nav.dagpenger.model.søknad.Seksjon
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

internal class EnumFaktaTest {

    enum class SpråkEnum {
        norsk, engelsk
    }

    private object språk {
        infix fun faktum(navn: String) = BaseFaktumFactory(SpråkEnum::class.java, navn)
    }

    @Test
    fun `enum-fakta`() {
        val søknad = Fakta(språk faktum "språk" id 1).søknad()
        val faktum = søknad.id(1) as Faktum<SpråkEnum>
        assertThrows<IllegalStateException> { faktum.svar() }
        faktum.besvar(SpråkEnum.engelsk)
        assertEquals(SpråkEnum.engelsk, faktum.svar())
    }

    @Test
    fun `subsumsjon test`() {
        val søknad = Fakta(språk faktum "språk" id 1).søknad()
        val faktum = søknad.id(1) as Faktum<SpråkEnum>
        val subsumsjon = faktum er SpråkEnum.engelsk

        assertEquals(null, subsumsjon.resultat())
        faktum.besvar(SpråkEnum.engelsk)
        assertEquals(true, subsumsjon.resultat())
    }
}
