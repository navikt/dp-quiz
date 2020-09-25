package no.nav.dagpenger.model.unit.visitor

import no.nav.dagpenger.model.fakta.Rolle
import no.nav.dagpenger.model.helpers.Eksempel
import no.nav.dagpenger.model.helpers.februar
import no.nav.dagpenger.model.helpers.januar
import no.nav.dagpenger.model.subsumsjon.Subsumsjon
import no.nav.dagpenger.model.subsumsjon.TomSubsumsjon
import no.nav.dagpenger.model.visitor.SubsumsjonJsonBuilder
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertNull

internal class MuligeSubsumsjonerTest {

    private lateinit var m: Eksempel
    private lateinit var rootSubsumsjon: Subsumsjon

    @BeforeEach
    fun setUp() {
        m = Eksempel()
        rootSubsumsjon = m.rootSubsumsjon
    }

    @Test
    fun `Fjerner umulig vei fra root`() {
        m.f1Boolean.besvar(false, Rolle.nav)
        m.f2Dato.besvar(1.februar, Rolle.nav)
        m.f3Dato.besvar(3.januar)
        m.f4Dato.besvar(4.januar)
        m.f5Dato.besvar(5.januar)
        val subsumsjon = rootSubsumsjon.mulige()
        val jsonBuilder = SubsumsjonJsonBuilder(subsumsjon)
        val json = jsonBuilder.resultat()

        assertEquals(TomSubsumsjon, subsumsjon.gyldig)

        assertNull(json["root"]["gyldig"])
    }
}
