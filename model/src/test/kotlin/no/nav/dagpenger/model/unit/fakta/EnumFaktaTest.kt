package no.nav.dagpenger.model.unit.fakta

import no.nav.dagpenger.model.fakta.FaktumNavn
import no.nav.dagpenger.model.fakta.Rolle
import no.nav.dagpenger.model.fakta.faktum
import no.nav.dagpenger.model.regel.er
import no.nav.dagpenger.model.søknad.Seksjon
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

internal class EnumFaktaTest {

    enum class SpråkEnum {
        norsk, engelsk
    }

    @Test
    fun `enum-fakta`() {
        val faktum = FaktumNavn<SpråkEnum>(1, "språk").faktum()
        val seksjon = Seksjon("seksjon", Rolle.søker, faktum)
        assertThrows<IllegalStateException> { faktum.svar() }
        faktum.besvar(SpråkEnum.engelsk)
        assertEquals(SpråkEnum.engelsk, faktum.svar())
    }

    @Test
    fun `subsumsjon test`() {
        val faktum = FaktumNavn<SpråkEnum>(1, "språk").faktum()
        val subsumsjon = faktum er SpråkEnum.engelsk
        val seksjon = Seksjon("seksjon", Rolle.søker, faktum)

        assertEquals(null, subsumsjon.resultat())

        faktum.besvar(SpråkEnum.engelsk)
        assertEquals(true, subsumsjon.resultat())
    }
}
