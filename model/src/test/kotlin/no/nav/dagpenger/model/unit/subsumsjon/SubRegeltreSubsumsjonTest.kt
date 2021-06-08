package no.nav.dagpenger.model.unit.subsumsjon

import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.boolsk
import no.nav.dagpenger.model.faktum.Faktum
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.helpers.testSøknadprosess
import no.nav.dagpenger.model.regel.er
import no.nav.dagpenger.model.subsumsjon.Subsumsjon
import no.nav.dagpenger.model.subsumsjon.hvisUgyldig
import no.nav.dagpenger.model.subsumsjon.medRegeltre
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class SubRegeltreSubsumsjonTest {
    private lateinit var f1: Faktum<Boolean>
    private lateinit var f2: Faktum<Boolean>
    private lateinit var subRegeltreSubsumsjon: Subsumsjon

    @BeforeEach
    fun setup() {
        val søknadprosess = Søknad(
            0,
            boolsk faktum "f1" id 1,
            boolsk faktum "f2" id 2
        ).testSøknadprosess()

        f1 = søknadprosess boolsk 1
        f2 = søknadprosess boolsk 2
        val s1 = f1 er true
        val s2 = f2 er true
        subRegeltreSubsumsjon = "regeltre" medRegeltre { s1 hvisUgyldig { s2 } }
    }

    @Test
    fun `Subregeltre resultat er lik child resultat`() {
        assertEquals(null, subRegeltreSubsumsjon.resultat())
        f1.besvar(true)
        assertEquals(true, subRegeltreSubsumsjon.resultat())
        f1.besvar(false)
        assertEquals(null, subRegeltreSubsumsjon.resultat())
        f2.besvar(true)
        assertEquals(true, subRegeltreSubsumsjon.resultat())
        f2.besvar(false)
        assertEquals(false, subRegeltreSubsumsjon.resultat())
    }
}
