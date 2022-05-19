package no.nav.dagpenger.quiz.mediator.soknad.dagpenger

import no.nav.dagpenger.model.faktum.Flervalg
import no.nav.dagpenger.model.faktum.Prosessversjon
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.faktum.Tekst
import no.nav.dagpenger.quiz.mediator.helpers.testSøknadprosess
import no.nav.dagpenger.quiz.mediator.soknad.Prosess
import no.nav.dagpenger.quiz.mediator.soknad.verifiserFeltsammensetting
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class ReellArbeidssokerTest {

    @Test
    fun `Sjekk om faktasammensettingen har endret seg siden sist`() {
        ReellArbeidssoker.verifiserFeltsammensetting(9, 45)
    }

    @Test
    fun `Kan kun jobbe deltid og ikke i hele Norge, ikke villig til å ta alle jobber eller gå ned i lønn`() {
        val søknad = Søknad(Prosessversjon(Prosess.Dagpenger, -1), *ReellArbeidssoker.fakta())
        val søknadprosess = søknad.testSøknadprosess(ReellArbeidssoker.regeltre(søknad))
        assertEquals(null, søknadprosess.resultat())

        søknadprosess.boolsk(ReellArbeidssoker.`Kan jobbe heltid`).besvar(false)
        assertEquals(null, søknadprosess.resultat())

        søknadprosess.flervalg(ReellArbeidssoker.`Årsak til kun deltid`).besvar(
            Flervalg("faktum.kun-deltid-aarsak.svar.omsorg-baby", "faktum.kun-deltid-aarsak.svar.annen-situasjon")
        )
        assertEquals(null, søknadprosess.resultat())

        søknadprosess.tekst(ReellArbeidssoker.`Skriv kort om situasjonen din`).besvar(Tekst("Jeg er omringet av maur"))
        assertEquals(null, søknadprosess.resultat())

        søknadprosess.heltall(ReellArbeidssoker.`Antall timer deltid du kan jobbe`).besvar(30)
        assertEquals(null, søknadprosess.resultat())

        søknadprosess.boolsk(ReellArbeidssoker.`Kan du jobbe i hele Norge`).besvar(false)
        assertEquals(null, søknadprosess.resultat())

        søknadprosess.flervalg(ReellArbeidssoker.`Årsak kan ikke jobbe i hele Norge`).besvar(
            Flervalg("faktum.ikke-jobbe-hele-norge.svar.redusert-helse", "faktum.ikke-jobbe-hele-norge.svar.annen-situasjon")
        )
        assertEquals(null, søknadprosess.resultat())

        søknadprosess.tekst(ReellArbeidssoker.`Kort om hvorfor ikke jobbe hele norge`).besvar(Tekst("Jeg er redd for hai"))
        assertEquals(null, søknadprosess.resultat())

        søknadprosess.boolsk(ReellArbeidssoker.`Kan ta alle typer arbeid`).besvar(false)
        assertEquals(null, søknadprosess.resultat())

        søknadprosess.boolsk(ReellArbeidssoker.`Kan bytte yrke og eller gå ned i lønn`).besvar(false)
        assertEquals(true, søknadprosess.resultat())
    }

    @Test
    fun `Kan jobbe heltid i hele Norge, jobbe med hva som helst, og gå ned i lønn`() {
        val søknad = Søknad(Prosessversjon(Prosess.Dagpenger, -1), *ReellArbeidssoker.fakta())
        val søknadprosess = søknad.testSøknadprosess(
            ReellArbeidssoker.regeltre(søknad)
        )

        søknadprosess.boolsk(ReellArbeidssoker.`Kan jobbe heltid`).besvar(true)
        assertEquals(null, søknadprosess.resultat())

        søknadprosess.boolsk(ReellArbeidssoker.`Kan du jobbe i hele Norge`).besvar(true)
        assertEquals(null, søknadprosess.resultat())

        søknadprosess.boolsk(ReellArbeidssoker.`Kan ta alle typer arbeid`).besvar(true)
        assertEquals(null, søknadprosess.resultat())

        søknadprosess.boolsk(ReellArbeidssoker.`Kan bytte yrke og eller gå ned i lønn`).besvar(true)
        assertEquals(true, søknadprosess.resultat())
    }
}
