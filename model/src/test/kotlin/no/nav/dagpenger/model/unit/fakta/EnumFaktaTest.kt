package no.nav.dagpenger.model.unit.fakta

import no.nav.dagpenger.model.regel.er
import no.nav.dagpenger.model.visitor.PrettyPrint
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

internal class EnumFaktaTest {

    enum class SpråkEnum {
        norsk, engelsk
    }

    @Test
    fun `enum-fakta`() {

        val faktum = "språk".faktum<SpråkEnum>()
        assertThrows<IllegalStateException> { faktum.svar() }
        faktum.besvar(SpråkEnum.engelsk)
        assertEquals(SpråkEnum.engelsk, faktum.svar())
    }

    @Test
    fun `subsumsjon test`() {
        val faktum = "språk".faktum<SpråkEnum>()

        val subsumsjon = faktum er SpråkEnum.engelsk
        assertEquals(null, subsumsjon.resultat())

        faktum.besvar(SpråkEnum.engelsk)
        assertEquals(true, subsumsjon.resultat())
        println(PrettyPrint(subsumsjon).result())

    }
}