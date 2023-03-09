package no.nav.dagpenger.model.unit.subsumsjon

import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.boolsk
import no.nav.dagpenger.model.faktum.Fakta
import no.nav.dagpenger.model.faktum.Faktum
import no.nav.dagpenger.model.helpers.testSøknadprosess
import no.nav.dagpenger.model.helpers.testversjon
import no.nav.dagpenger.model.regel.er
import no.nav.dagpenger.model.subsumsjon.Subsumsjon
import no.nav.dagpenger.model.subsumsjon.deltre
import no.nav.dagpenger.model.subsumsjon.hvisIkkeOppfylt
import no.nav.dagpenger.model.subsumsjon.hvisOppfylt
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class DeltreSubsumsjonTest {
    private lateinit var faktum1: Faktum<Boolean>
    private lateinit var faktum2: Faktum<Boolean>
    private lateinit var faktum3: Faktum<Boolean>
    private lateinit var deltre: Subsumsjon

    @BeforeEach
    fun setup() {
        val søknadprosess = Fakta(
            testversjon,
            boolsk faktum "faktum1" id 1,
            boolsk faktum "faktum2" id 2,
            boolsk faktum "faktum3" id 3
        ).testSøknadprosess()

        faktum1 = søknadprosess boolsk 1
        faktum2 = søknadprosess boolsk 2
        faktum3 = søknadprosess boolsk 3
        val subsumsjon1 = faktum1 er true
        val subsumsjon2 = faktum2 er true
        val subsumsjon3 = faktum3 er true
        deltre = "deltre" deltre { subsumsjon1 hvisOppfylt { subsumsjon3 } hvisIkkeOppfylt { subsumsjon2 } }
    }

    @Test
    fun `deltre resultat er lik child resultat`() {
        assertEquals(null, deltre.resultat())
        faktum1.besvar(true)
        assertEquals(null, deltre.resultat())
        faktum3.besvar(true)
        assertEquals(true, deltre.resultat())
        faktum3.besvar(false)
        assertEquals(false, deltre.resultat())

        faktum1.besvar(false)
        assertEquals(null, deltre.resultat())
        faktum2.besvar(true)
        assertEquals(true, deltre.resultat())
        faktum2.besvar(false)
        assertEquals(false, deltre.resultat())
    }
}
