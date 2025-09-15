package no.nav.dagpenger.model.unit.subsumsjon

import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.boolsk
import no.nav.dagpenger.model.faktum.Fakta
import no.nav.dagpenger.model.helpers.testSøknadprosess
import no.nav.dagpenger.model.helpers.testversjon
import no.nav.dagpenger.model.regel.er
import no.nav.dagpenger.model.subsumsjon.bareEnAv
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class BareEnAvSubsumsjonTest {
    private val fakta =
        Fakta(
            testversjon,
            boolsk faktum "neida" id 1,
            boolsk faktum "joda" id 2,
            boolsk faktum "ja" id 3,
        )

    private val neida = fakta boolsk 1
    private val joda = fakta boolsk 2
    private val ja1 = fakta boolsk 3
    private val bareEnAv =
        "Enten joda eller neida".bareEnAv(
            neida er true,
            joda er true,
            ja1 er true,
        )

    @Test
    fun `skal være true bare hvis en av undersubsumsjonene er true`() {
        val søknadsprosess = fakta.testSøknadprosess(bareEnAv)
        søknadsprosess.boolsk(1).besvar(true)
        søknadsprosess.boolsk(2).besvar(false)
        søknadsprosess.boolsk(3).besvar(false)
        assertEquals(true, søknadsprosess.resultat())

        søknadsprosess.boolsk(2).besvar(true)
        assertEquals(false, søknadsprosess.resultat())
    }

    @Test
    fun `bareEnAv sjekker alle undersubsumsjoner, resultatet er ikke klart før alle er besvart`() {
        val søknadsprosess = fakta.testSøknadprosess(bareEnAv)
        søknadsprosess.boolsk(1).besvar(false)
        assertNull(søknadsprosess.resultat())
    }
}
