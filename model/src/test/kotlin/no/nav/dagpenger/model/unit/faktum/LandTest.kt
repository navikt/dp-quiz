package no.nav.dagpenger.model.unit.faktum

import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.land
import no.nav.dagpenger.model.faktum.Land
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.helpers.testSøknadprosess
import no.nav.dagpenger.model.helpers.testversjon
import no.nav.dagpenger.model.regel.erEnDelAv
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class LandTest {
    @Test
    fun validation() {
        assertThrows<IllegalArgumentException> { Land("NORGE") }
        assertThrows<IllegalArgumentException> { Land("DDD") }
    }

    @Test
    fun equality() {
        assertEquals(Land("NOR"), Land("NOR"))
        Land("SWE").let { land ->
            assertEquals(land, land)
        }
        assertNotEquals(Land("SWE"), Land("NOR"))
    }

    @Test
    fun `case insentivity`() {
        assertEquals(Land("NOR"), Land("nor"))
        assertEquals(Land("nOR"), Land("Nor"))
    }

    @Test
    fun `er en del av`() {
        val landListe = listOf(Land("NOR"), Land("SWE"))
        val søknad = Søknad(
            testversjon,
            land faktum "land" id 1,
        )

        val søknadsprosess = søknad.testSøknadprosess(
            søknad land 1 erEnDelAv landListe
        )

        assertNull(søknadsprosess.rootSubsumsjon.resultat())
        søknadsprosess.land(1).besvar(Land("DEU"))
        assertFalse(søknadsprosess.rootSubsumsjon.resultat()!!)

        søknadsprosess.land(1).besvar(Land("NOR"))
        assertTrue(søknadsprosess.rootSubsumsjon.resultat()!!)

        søknadsprosess.land(1).besvar(Land("SWE"))
        assertTrue(søknadsprosess.rootSubsumsjon.resultat()!!)
    }
}
