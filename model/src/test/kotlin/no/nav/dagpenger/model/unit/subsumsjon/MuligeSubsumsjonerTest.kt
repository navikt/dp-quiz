package no.nav.dagpenger.model.unit.subsumsjon

import no.nav.dagpenger.model.faktagrupper.Faktagrupper
import no.nav.dagpenger.model.helpers.NyttEksempel
import no.nav.dagpenger.model.helpers.februar
import no.nav.dagpenger.model.helpers.januar
import no.nav.dagpenger.model.marshalling.SubsumsjonJsonBuilder
import no.nav.dagpenger.model.subsumsjon.Subsumsjon
import no.nav.dagpenger.model.subsumsjon.TomSubsumsjon
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertNull

internal class MuligeSubsumsjonerTest {

    private lateinit var m: NyttEksempel
    private lateinit var rootSubsumsjon: Subsumsjon
    private lateinit var faktagrupper: Faktagrupper

    @BeforeEach
    fun setUp() {
        m = NyttEksempel()
        faktagrupper = m.faktagrupper
        rootSubsumsjon = faktagrupper.rootSubsumsjon
    }

    @Test
    fun `Fjerner umulig gyldig vei fra root`() {
        faktagrupper.ja(1).besvar(false)
        faktagrupper.dato(2).besvar(1.februar)
        faktagrupper.dato(3).besvar(3.januar)
        faktagrupper.dato(4).besvar(4.januar)
        faktagrupper.dato(5).besvar(5.januar)

        val jsonBuilder = SubsumsjonJsonBuilder.mulige(rootSubsumsjon)
        val json = jsonBuilder.resultat()

        assertEquals(TomSubsumsjon, rootSubsumsjon.mulige().gyldig)
        assertNull(json["root"]["gyldig"])
    }

    @Test
    fun `Fjerner umulig ugyldig vei fra root`() {

        faktagrupper.ja(10).besvar(true)

        val jsonBuilder = SubsumsjonJsonBuilder.mulige(rootSubsumsjon)
        val json = jsonBuilder.resultat()

        assertNull(json["root"]["gyldig"]["ugyldig"])
    }
}
