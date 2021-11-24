package no.nav.dagpenger.model.unit.subsumsjon

import no.nav.dagpenger.model.factory.BaseFaktumFactory
import no.nav.dagpenger.model.faktum.ProsessVersjon
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.helpers.testSøknadprosess
import no.nav.dagpenger.model.regel.er
import no.nav.dagpenger.model.subsumsjon.minstEnAv
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

internal class MinstEnAvSubsumsjonTest {

    private val søknad = Søknad(
        ProsessVersjon("test", 0),
        BaseFaktumFactory.Companion.boolsk faktum "neida" id 1,
        BaseFaktumFactory.Companion.boolsk faktum "joda" id 2,
        BaseFaktumFactory.Companion.boolsk faktum "ja" id 3
    )

    private val neida = søknad boolsk 1
    private val joda = søknad boolsk 2
    private val ja1 = søknad boolsk 3
    private val minstEnAv = "Enten joda eller neida".minstEnAv(
        neida er true,
        joda er true,
        ja1 er true
    )

    @Test
    fun `skal være true hvis resultatet av minst én undersubsumsjon er true`() {

        val søknadsprosess = søknad.testSøknadprosess(minstEnAv)
        søknadsprosess.boolsk(1).besvar(false)
        søknadsprosess.boolsk(2).besvar(true)
        søknadsprosess.boolsk(3).besvar(false)
        assertEquals(true, søknadsprosess.resultat())

        søknadsprosess.boolsk(2).besvar(false)
        assertEquals(false, søknadsprosess.resultat())
    }

    @Test
    fun `minstEnAv sjekker alle undersubsumsjoner, resultatet er ikke klart før alle er besvart`() {
        val søknadsprosess = søknad.testSøknadprosess(minstEnAv)
        søknadsprosess.boolsk(1).besvar(false)
        assertNull(søknadsprosess.resultat())
    }
}
