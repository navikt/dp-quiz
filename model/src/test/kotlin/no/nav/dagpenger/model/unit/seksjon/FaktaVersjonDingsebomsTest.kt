package no.nav.dagpenger.model.unit.seksjon

import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.boolsk
import no.nav.dagpenger.model.faktum.Fakta
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.helpers.TestProsesser
import no.nav.dagpenger.model.helpers.testPerson
import no.nav.dagpenger.model.helpers.testversjon
import no.nav.dagpenger.model.regel.er
import no.nav.dagpenger.model.seksjon.FaktaVersjonDingseboms
import no.nav.dagpenger.model.seksjon.Prosess
import no.nav.dagpenger.model.seksjon.Seksjon
import org.junit.jupiter.api.Test

class FaktaVersjonDingsebomsTest {

    private val prototypeFakta = Fakta(
        testversjon,
        boolsk faktum "f1" id 1
    )

    private val prototypeProsess = Prosess(
        TestProsesser.Test,
        Seksjon("s1", Rolle.søker, prototypeFakta.boolsk("f1"))
    )

    private val regeltre = with(prototypeFakta) {
        boolsk("f1") er true
    }

    @Test
    fun `kan opprette nye fakta og ny prosess`() {

        val faktaBygger = FaktaVersjonDingseboms.Bygger(
            prototypeFakta
        ).also { bygger ->
            bygger.leggTilProsess(prototypeProsess, regeltre)
        }

        val prosess = faktaBygger.prosess(testPerson, TestProsesser.Test)

        prosess.boolsk("f1").besvar(true)
    }
}
