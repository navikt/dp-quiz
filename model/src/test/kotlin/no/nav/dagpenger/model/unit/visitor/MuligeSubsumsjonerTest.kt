package no.nav.dagpenger.model.unit.visitor

import no.nav.dagpenger.model.fakta.Rolle
import no.nav.dagpenger.model.helpers.NyEksempel
import no.nav.dagpenger.model.helpers.februar
import no.nav.dagpenger.model.helpers.januar
import no.nav.dagpenger.model.subsumsjon.Subsumsjon
import no.nav.dagpenger.model.subsumsjon.TomSubsumsjon
import no.nav.dagpenger.model.søknad.Søknad
import no.nav.dagpenger.model.visitor.SubsumsjonJsonBuilder
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertNull

internal class MuligeSubsumsjonerTest {

    private lateinit var m: NyEksempel
    private lateinit var rootSubsumsjon: Subsumsjon
    private lateinit var søknad: Søknad

    @BeforeEach
    fun setUp() {
        m = NyEksempel()
        søknad = m.søknad
        rootSubsumsjon = søknad.rootSubsumsjon
    }

    @Test
    fun `Fjerner umulig gyldig vei fra root`() {
        søknad.ja(1).besvar(false, Rolle.nav)
        søknad.dato(2).besvar(1.februar, Rolle.nav)
        søknad.dato(3).besvar(3.januar)
        søknad.dato(4).besvar(4.januar)
        søknad.dato(5).besvar(5.januar)

        val jsonBuilder = SubsumsjonJsonBuilder.mulige(rootSubsumsjon)
        val json = jsonBuilder.resultat()

        assertEquals(TomSubsumsjon, rootSubsumsjon.mulige().gyldig)
        assertNull(json["root"]["gyldig"])
    }

    @Test
    fun `Fjerner umulig ugyldig vei fra root`() {

        søknad.ja(10).besvar(true)

        val jsonBuilder = SubsumsjonJsonBuilder.mulige(rootSubsumsjon)
        val json = jsonBuilder.resultat()

        assertNull(json["root"]["gyldig"]["ugyldig"])
    }
}
