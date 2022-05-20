package no.nav.dagpenger.quiz.mediator.soknad.dagpenger

import no.nav.dagpenger.model.faktum.Land
import no.nav.dagpenger.model.faktum.Periode
import no.nav.dagpenger.model.faktum.Prosessversjon
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.faktum.Tekst
import no.nav.dagpenger.quiz.mediator.helpers.testSøknadprosess
import no.nav.dagpenger.quiz.mediator.soknad.Prosess
import no.nav.dagpenger.quiz.mediator.soknad.verifiserFeltsammensetting
import org.junit.jupiter.api.Test
import java.time.LocalDate
import kotlin.test.assertEquals

internal class BostedTest {

    @Test
    fun `Sjekk om faktasammensettingen har endret seg siden sist`() {
        Bosted.verifiserFeltsammensetting(6, 36021)
    }

    @Test
    fun `Bostedsregel for Norge, Svalbard og Jan Mayen`() {
        val søknad = Søknad(Prosessversjon(Prosess.Dagpenger, -1), *Bosted.fakta())
        val søknadprosess = søknad.testSøknadprosess(
            Bosted.regeltre(søknad)
        )

        søknadprosess.land(Bosted.`hvilket land bor du i`).besvar(Land("NOR"))
        assertEquals(true, søknadprosess.resultat())

        søknadprosess.land(Bosted.`hvilket land bor du i`).besvar(Land("SJM"))
        assertEquals(true, søknadprosess.resultat())
    }

    @Test
    fun `Bostedsregel for EØS og Sveits ikke reist tilbake `() {
        val søknad = Søknad(Prosessversjon(Prosess.Dagpenger, -1), *Bosted.fakta())
        val søknadprosess = søknad.testSøknadprosess(
            Bosted.regeltre(søknad)
        )

        forventedeEøsLand().forEach { land ->
            søknadprosess.land(Bosted.`hvilket land bor du i`).besvar(land)
            søknadprosess.boolsk(Bosted.`reist tilbake etter arbeidsledig`).besvar(false)
            søknadprosess.boolsk(Bosted.`reist tilbake en gang i uka eller mer`).besvar(true)
            assertEquals(true, søknadprosess.resultat())

            søknadprosess.boolsk(Bosted.`reist tilbake en gang i uka eller mer`).besvar(false)
            assertEquals(null, søknadprosess.resultat())
            søknadprosess.boolsk(Bosted.`reist i takt med rotasjon`).besvar(false)
            assertEquals(true, søknadprosess.resultat())
            søknadprosess.boolsk(Bosted.`reist i takt med rotasjon`).besvar(true)
            assertEquals(true, søknadprosess.resultat())
        }
    }
    @Test
    fun `Bostedsregel for EØS og Sveits og har reist tilbake til bostedslandet`() {
        val søknad = Søknad(Prosessversjon(Prosess.Dagpenger, -1), *Bosted.fakta())
        val søknadprosess = søknad.testSøknadprosess(
            Bosted.regeltre(søknad)
        )

        forventedeEøsLand().forEach { land ->
            søknadprosess.land(Bosted.`hvilket land bor du i`).besvar(land)
            søknadprosess.boolsk(Bosted.`reist tilbake etter arbeidsledig`).besvar(true)
            assertEquals(null, søknadprosess.resultat())
            søknadprosess.periode(Bosted.`reist tilbake periode`).besvar(
                Periode(
                    fom = LocalDate.now().minusDays(20),
                    tom = LocalDate.now()
                )
            )
            assertEquals(null, søknadprosess.resultat())
            søknadprosess.tekst(Bosted.`reist tilbake aarsak`).besvar(Tekst("Varmt vann og SOL i utlandet"))
            assertEquals(null, søknadprosess.resultat())
            søknadprosess.boolsk(Bosted.`reist tilbake en gang i uka eller mer`).besvar(true)
            assertEquals(true, søknadprosess.resultat())
            søknadprosess.boolsk(Bosted.`reist tilbake en gang i uka eller mer`).besvar(false)
            assertEquals(null, søknadprosess.resultat())
            søknadprosess.boolsk(Bosted.`reist i takt med rotasjon`).besvar(false)
            assertEquals(true, søknadprosess.resultat())
            søknadprosess.boolsk(Bosted.`reist i takt med rotasjon`).besvar(true)
            assertEquals(true, søknadprosess.resultat())
        }
    }

    @Test
    fun `Bostedsregel for Storbritannia`() {
        val søknad = Søknad(Prosessversjon(Prosess.Dagpenger, -1), *Bosted.fakta())
        val søknadprosess = søknad.testSøknadprosess(
            Bosted.regeltre(søknad)
        )

        søknadprosess.land(Bosted.`hvilket land bor du i`).besvar(Land("GBR"))
        assertEquals(true, søknadprosess.resultat())

        søknadprosess.land(Bosted.`hvilket land bor du i`).besvar(Land("JEY"))
        assertEquals(true, søknadprosess.resultat())

        søknadprosess.land(Bosted.`hvilket land bor du i`).besvar(Land("IMN"))
        assertEquals(true, søknadprosess.resultat())
    }

    @Test
    fun `Bostedsregel for utenfor EØS`() {
        val søknad = Søknad(Prosessversjon(Prosess.Dagpenger, -1), *Bosted.fakta())
        val søknadprosess = søknad.testSøknadprosess(
            Bosted.regeltre(søknad)
        )

        søknadprosess.land(Bosted.`hvilket land bor du i`).besvar(Land("AUS"))
        assertEquals(true, søknadprosess.resultat())
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