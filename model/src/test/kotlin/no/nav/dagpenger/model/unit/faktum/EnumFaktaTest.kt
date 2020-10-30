package no.nav.dagpenger.model.unit.faktum

import no.nav.dagpenger.model.factory.BaseFaktumFactory
import no.nav.dagpenger.model.faktum.Faktum
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.helpers.testSøknad
import no.nav.dagpenger.model.regel.er
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
        val faktagrupper = Søknad(språk faktum "språk" id 1).testSøknad()
        val faktum = faktagrupper.id(1) as Faktum<SpråkEnum>
        assertThrows<IllegalStateException> { faktum.svar() }
        faktum.besvar(SpråkEnum.engelsk)
        assertEquals(SpråkEnum.engelsk, faktum.svar())
    }

    @Test
    fun `subsumsjon test`() {
        val faktagrupper = Søknad(språk faktum "språk" id 1).testSøknad()
        val faktum = faktagrupper.id(1) as Faktum<SpråkEnum>
        val subsumsjon = faktum er SpråkEnum.engelsk

        assertEquals(null, subsumsjon.resultat())
        faktum.besvar(SpråkEnum.engelsk)
        assertEquals(true, subsumsjon.resultat())
    }
}
