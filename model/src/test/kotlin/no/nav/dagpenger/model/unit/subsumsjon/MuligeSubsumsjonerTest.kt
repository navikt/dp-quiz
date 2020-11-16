package no.nav.dagpenger.model.unit.subsumsjon

import no.nav.dagpenger.model.helpers.NyttEksempel
import no.nav.dagpenger.model.helpers.februar
import no.nav.dagpenger.model.helpers.januar
import no.nav.dagpenger.model.marshalling.SubsumsjonJsonBuilder
import no.nav.dagpenger.model.seksjon.Søknadprosess
import no.nav.dagpenger.model.subsumsjon.Subsumsjon
import no.nav.dagpenger.model.subsumsjon.TomSubsumsjon
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertNull

internal class MuligeSubsumsjonerTest {

    private lateinit var m: NyttEksempel
    private lateinit var rootSubsumsjon: Subsumsjon
    private lateinit var søknadprosess: Søknadprosess

    @BeforeEach
    fun setUp() {
        m = NyttEksempel()
        søknadprosess = m.søknadprosess
        rootSubsumsjon = søknadprosess.rootSubsumsjon
    }

    @Test
    fun `Fjerner umulig gyldig vei fra root`() {
        søknadprosess.ja(1).besvar(false)
        søknadprosess.dato(2).besvar(1.februar)
        søknadprosess.dato(3).besvar(3.januar)
        søknadprosess.dato(4).besvar(4.januar)
        søknadprosess.dato(5).besvar(5.januar)

        val jsonBuilder = SubsumsjonJsonBuilder.mulige(rootSubsumsjon)
        val json = jsonBuilder.resultat()

        assertEquals(TomSubsumsjon, rootSubsumsjon.mulige().gyldig)
        assertNull(json["root"]["gyldig"])
    }

    @Test
    fun `Fjerner umulig ugyldig vei fra root`() {

        søknadprosess.ja(10).besvar(true)

        val jsonBuilder = SubsumsjonJsonBuilder.mulige(rootSubsumsjon)
        val json = jsonBuilder.resultat()

        assertNull(json["root"]["gyldig"]["ugyldig"])
    }
}
