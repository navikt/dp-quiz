package no.nav.dagpenger.quiz.mediator.soknad.dagpenger

import no.nav.dagpenger.model.faktum.Land
import no.nav.dagpenger.model.faktum.Prosessversjon
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.quiz.mediator.helpers.testSøknadprosess
import no.nav.dagpenger.quiz.mediator.soknad.Prosess
import no.nav.dagpenger.quiz.mediator.soknad.verifiserFeltsammensetting
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class BostedTest {

    @Test
    fun `Sjekk om faktasammensettingen har endret seg siden sist`() {
        Bosted.verifiserFeltsammensetting(6, 36021)
    }

    @Test
    fun `Test regeltreet`() {
        val søknad = Søknad(Prosessversjon(Prosess.Dagpenger, -1), *Bosted.fakta())
        val søknadprosess = søknad.testSøknadprosess(
            Bosted.regeltre(søknad)
        )

        søknadprosess.land(Bosted.`hvilket land bor du i`).besvar(Land("NOR"))
        assertEquals(true, søknadprosess.resultat())

        søknadprosess.land(Bosted.`hvilket land bor du i`).besvar(Land("SJM"))
        assertEquals(true, søknadprosess.resultat())

        forventedeEøsLand().forEach { land ->
            søknadprosess.land(Bosted.`hvilket land bor du i`).besvar(land)
            assertEquals(true, søknadprosess.resultat())
        }
    }

    private fun forventedeEøsLand() = listOf(
        "BEL",
        "BGR",
        "DNK",
        "EST",
        "FIN",
        "FRA",
        "GRC",
        "IRL",
        "ISL",
        "ITA",
        "HRV",
        "CYP",
        "LVA",
        "LIE",
        "LTU",
        "LUX",
        "MLT",
        "NLD",
        "POL",
        "PRT",
        "ROU",
        "SVK",
        "SVN",
        "ESP",
        "CHE",
        "SWE",
        "CZE",
        "DEU",
        "HUN",
        "AUT"
    ).map { land ->
        Land(land)
    }
}
