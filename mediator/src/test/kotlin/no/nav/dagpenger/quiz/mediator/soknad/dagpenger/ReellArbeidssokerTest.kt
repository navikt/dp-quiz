package no.nav.dagpenger.quiz.mediator.soknad.dagpenger

import no.nav.dagpenger.model.faktum.Faktum
import no.nav.dagpenger.model.faktum.Flervalg
import no.nav.dagpenger.model.faktum.Prosessversjon
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.faktum.Tekst
import no.nav.dagpenger.model.seksjon.Søknadprosess
import no.nav.dagpenger.quiz.mediator.helpers.testSøknadprosess
import no.nav.dagpenger.quiz.mediator.soknad.Prosess
import no.nav.dagpenger.quiz.mediator.soknad.dagpenger.ReellArbeidssoker.`Antall timer deltid du kan jobbe`
import no.nav.dagpenger.quiz.mediator.soknad.dagpenger.ReellArbeidssoker.`Kan bytte yrke og eller gå ned i lønn`
import no.nav.dagpenger.quiz.mediator.soknad.dagpenger.ReellArbeidssoker.`Kan du jobbe i hele Norge`
import no.nav.dagpenger.quiz.mediator.soknad.dagpenger.ReellArbeidssoker.`Kan jobbe heltid`
import no.nav.dagpenger.quiz.mediator.soknad.dagpenger.ReellArbeidssoker.`Kan ta alle typer arbeid`
import no.nav.dagpenger.quiz.mediator.soknad.dagpenger.ReellArbeidssoker.`Kort om hvorfor ikke jobbe hele norge`
import no.nav.dagpenger.quiz.mediator.soknad.dagpenger.ReellArbeidssoker.`Skriv kort om situasjonen din`
import no.nav.dagpenger.quiz.mediator.soknad.dagpenger.ReellArbeidssoker.`Årsak kan ikke jobbe i hele Norge`
import no.nav.dagpenger.quiz.mediator.soknad.dagpenger.ReellArbeidssoker.`Årsak til kun deltid`
import no.nav.dagpenger.quiz.mediator.soknad.verifiserFeltsammensetting
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class ReellArbeidssokerTest {

    @Test
    fun `Sjekk om faktasammensettingen har endret seg siden sist`() {
        ReellArbeidssoker.verifiserFeltsammensetting(9, 45)
    }

    @Test
    fun `Fakta om hvorfor deltid blir besvart`() {
        val søknad = Søknad(Prosessversjon(Prosess.Dagpenger, -1), *ReellArbeidssoker.fakta())
        val søknadprosess = søknad.testSøknadprosess(ReellArbeidssoker.regeltre(søknad))

        `Besvar alle fakta hvorfor deltid`(søknadprosess)

        assertFaktaErBesvart(
            søknadprosess.boolsk(`Kan jobbe heltid`),
            søknadprosess.flervalg(`Årsak til kun deltid`),
            søknadprosess.tekst(`Skriv kort om situasjonen din`),
            søknadprosess.heltall(`Antall timer deltid du kan jobbe`)
        )
    }

    @Test
    fun `Fakta om hvorfor ikke jobbe i hele Norge blir besvart`() {
        val søknad = Søknad(Prosessversjon(Prosess.Dagpenger, -1), *ReellArbeidssoker.fakta())
        val søknadprosess = søknad.testSøknadprosess(ReellArbeidssoker.regeltre(søknad))

        `Besvar alle fakta hvorfor ikke jobbe i hele Norge`(søknadprosess)

        assertFaktaErBesvart(
            søknadprosess.boolsk(`Kan du jobbe i hele Norge`),
            søknadprosess.flervalg(`Årsak kan ikke jobbe i hele Norge`),
            søknadprosess.tekst(`Kort om hvorfor ikke jobbe hele norge`)
        )
    }

    @Test
    fun `Fakta om deltid blir riktig invalidert`() {
        val søknad = Søknad(Prosessversjon(Prosess.Dagpenger, -1), *ReellArbeidssoker.fakta())
        val søknadprosess = søknad.testSøknadprosess(ReellArbeidssoker.regeltre(søknad))

        `Besvar alle fakta hvorfor deltid`(søknadprosess)
        søknadprosess.boolsk(`Kan jobbe heltid`).besvar(true)

        assertAvhengigeFaktaInvalideres(
            søknadprosess.flervalg(`Årsak til kun deltid`),
            søknadprosess.tekst(`Skriv kort om situasjonen din`),
            søknadprosess.heltall(`Antall timer deltid du kan jobbe`)
        )

        `Besvar alle fakta hvorfor deltid`(søknadprosess)
        søknadprosess.flervalg(`Årsak til kun deltid`).besvar(Flervalg("faktum.kun-deltid-aarsak.svar.annen-situasjon"))

        assertAvhengigeFaktaInvalideres(
            søknadprosess.tekst(`Skriv kort om situasjonen din`),
            søknadprosess.heltall(`Antall timer deltid du kan jobbe`)
        )

        `Besvar alle fakta hvorfor deltid`(søknadprosess)
        søknadprosess.tekst(`Skriv kort om situasjonen din`).besvar(Tekst("Noe greier"))

        assertAvhengigeFaktaInvalideres(
            søknadprosess.heltall(`Antall timer deltid du kan jobbe`)
        )
    }

    @Test
    fun `Kan kun jobbe deltid og ikke i hele Norge, ikke villig til å ta alle jobber eller gå ned i lønn`() {
        val søknad = Søknad(Prosessversjon(Prosess.Dagpenger, -1), *ReellArbeidssoker.fakta())
        val søknadprosess = søknad.testSøknadprosess(ReellArbeidssoker.regeltre(søknad))
        assertEquals(null, søknadprosess.resultat())

        søknadprosess.boolsk(`Kan jobbe heltid`).besvar(false)
        assertEquals(null, søknadprosess.resultat())

        søknadprosess.flervalg(`Årsak til kun deltid`).besvar(
            Flervalg("faktum.kun-deltid-aarsak.svar.omsorg-baby", "faktum.kun-deltid-aarsak.svar.annen-situasjon")
        )
        assertEquals(null, søknadprosess.resultat())

        søknadprosess.tekst(`Skriv kort om situasjonen din`).besvar(Tekst("Jeg er omringet av maur"))
        assertEquals(null, søknadprosess.resultat())

        søknadprosess.heltall(`Antall timer deltid du kan jobbe`).besvar(30)
        assertEquals(null, søknadprosess.resultat())

        søknadprosess.boolsk(`Kan du jobbe i hele Norge`).besvar(false)
        assertEquals(null, søknadprosess.resultat())

        søknadprosess.flervalg(`Årsak kan ikke jobbe i hele Norge`).besvar(
            Flervalg("faktum.ikke-jobbe-hele-norge.svar.redusert-helse", "faktum.ikke-jobbe-hele-norge.svar.annen-situasjon")
        )
        assertEquals(null, søknadprosess.resultat())

        søknadprosess.tekst(`Kort om hvorfor ikke jobbe hele norge`).besvar(Tekst("Jeg er redd for hai"))
        assertEquals(null, søknadprosess.resultat())

        søknadprosess.boolsk(`Kan ta alle typer arbeid`).besvar(false)
        assertEquals(null, søknadprosess.resultat())

        søknadprosess.boolsk(`Kan bytte yrke og eller gå ned i lønn`).besvar(false)
        assertEquals(true, søknadprosess.resultat())

        assertFaktaErBesvart(
            søknadprosess.boolsk(`Kan jobbe heltid`),
            søknadprosess.flervalg(`Årsak til kun deltid`),
            søknadprosess.tekst(`Skriv kort om situasjonen din`),
            søknadprosess.heltall(`Antall timer deltid du kan jobbe`),
            søknadprosess.boolsk(`Kan du jobbe i hele Norge`),
            søknadprosess.flervalg(`Årsak kan ikke jobbe i hele Norge`),
            søknadprosess.tekst(`Kort om hvorfor ikke jobbe hele norge`),
            søknadprosess.boolsk(`Kan ta alle typer arbeid`),
            søknadprosess.boolsk(`Kan bytte yrke og eller gå ned i lønn`)
        )
    }

    @Test
    fun `Kan jobbe heltid i hele Norge, jobbe med hva som helst, og gå ned i lønn`() {
        val søknad = Søknad(Prosessversjon(Prosess.Dagpenger, -1), *ReellArbeidssoker.fakta())
        val søknadprosess = søknad.testSøknadprosess(
            ReellArbeidssoker.regeltre(søknad)
        )

        søknadprosess.boolsk(`Kan jobbe heltid`).besvar(true)
        assertEquals(null, søknadprosess.resultat())

        søknadprosess.boolsk(`Kan du jobbe i hele Norge`).besvar(true)
        assertEquals(null, søknadprosess.resultat())

        søknadprosess.boolsk(`Kan ta alle typer arbeid`).besvar(true)
        assertEquals(null, søknadprosess.resultat())

        søknadprosess.boolsk(`Kan bytte yrke og eller gå ned i lønn`).besvar(true)
        assertEquals(true, søknadprosess.resultat())

        assertFaktaErBesvart(
            søknadprosess.boolsk(`Kan jobbe heltid`),
            søknadprosess.boolsk(`Kan du jobbe i hele Norge`),
            søknadprosess.boolsk(`Kan ta alle typer arbeid`),
            søknadprosess.boolsk(`Kan bytte yrke og eller gå ned i lønn`)
        )
    }

    private fun `Besvar alle fakta hvorfor deltid`(søknadprosess: Søknadprosess) {
        søknadprosess.boolsk(`Kan jobbe heltid`).besvar(false)
        søknadprosess.flervalg(`Årsak til kun deltid`).besvar(
            Flervalg("faktum.kun-deltid-aarsak.svar.annen-situasjon")
        )
        søknadprosess.tekst(`Skriv kort om situasjonen din`).besvar(Tekst("Jeg er omringet av maur"))
        søknadprosess.heltall(`Antall timer deltid du kan jobbe`).besvar(20)
    }

    private fun `Besvar alle fakta hvorfor ikke jobbe i hele Norge`(søknadprosess: Søknadprosess) {
        `Besvar alle fakta hvorfor deltid`(søknadprosess)
        søknadprosess.boolsk(`Kan du jobbe i hele Norge`).besvar(false)
        søknadprosess.flervalg(`Årsak kan ikke jobbe i hele Norge`).besvar(
            Flervalg("faktum.ikke-jobbe-hele-norge.svar.redusert-helse", "faktum.ikke-jobbe-hele-norge.svar.annen-situasjon")
        )
        søknadprosess.tekst(`Kort om hvorfor ikke jobbe hele norge`).besvar(Tekst("Jeg er redd for hai"))
    }

    private fun assertFaktaErBesvart(vararg fakta: Faktum<*>) {
        fakta.forEach { faktum ->
            assertEquals(true, faktum.erBesvart())
        }
    }

    private fun assertAvhengigeFaktaInvalideres(vararg fakta: Faktum<*>) {
        fakta.forEach { faktum ->
            assertEquals(false, faktum.erBesvart())
        }
    }
}
