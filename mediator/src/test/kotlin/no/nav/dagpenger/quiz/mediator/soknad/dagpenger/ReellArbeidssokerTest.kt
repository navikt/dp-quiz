package no.nav.dagpenger.quiz.mediator.soknad.dagpenger

import no.nav.dagpenger.model.faktum.Faktum
import no.nav.dagpenger.model.faktum.Flervalg
import no.nav.dagpenger.model.faktum.Prosessversjon
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.faktum.Tekst
import no.nav.dagpenger.model.seksjon.Søknadprosess
import no.nav.dagpenger.quiz.mediator.helpers.testSøknadprosess
import no.nav.dagpenger.quiz.mediator.soknad.Prosess
import no.nav.dagpenger.quiz.mediator.soknad.dagpenger.ReellArbeidssoker.`antall timer deltid du kan jobbe`
import no.nav.dagpenger.quiz.mediator.soknad.dagpenger.ReellArbeidssoker.`kan bytte yrke eller gå ned i lønn`
import no.nav.dagpenger.quiz.mediator.soknad.dagpenger.ReellArbeidssoker.`kan du jobbe i hele Norge`
import no.nav.dagpenger.quiz.mediator.soknad.dagpenger.ReellArbeidssoker.`kan jobbe heltid`
import no.nav.dagpenger.quiz.mediator.soknad.dagpenger.ReellArbeidssoker.`kan ta alle typer arbeid`
import no.nav.dagpenger.quiz.mediator.soknad.dagpenger.ReellArbeidssoker.`kort om hvorfor ikke jobbe hele norge`
import no.nav.dagpenger.quiz.mediator.soknad.dagpenger.ReellArbeidssoker.`skriv kort om situasjonen din`
import no.nav.dagpenger.quiz.mediator.soknad.dagpenger.ReellArbeidssoker.`årsak kan ikke jobbe i hele Norge`
import no.nav.dagpenger.quiz.mediator.soknad.dagpenger.ReellArbeidssoker.`årsak til kun deltid`
import no.nav.dagpenger.quiz.mediator.soknad.verifiserFeltsammensetting
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class ReellArbeidssokerTest {

    private lateinit var søknad: Søknad
    private lateinit var søknadprosess: Søknadprosess

    @BeforeEach
    fun setup() {
        søknad = Søknad(Prosessversjon(Prosess.Dagpenger, -1), *ReellArbeidssoker.fakta())
        søknadprosess = søknad.testSøknadprosess(ReellArbeidssoker.regeltre(søknad)) {
            ReellArbeidssoker.seksjon(this)
        }
    }

    @Test
    fun `Sjekk om faktasammensettingen har endret seg siden sist`() {
        ReellArbeidssoker.verifiserFeltsammensetting(29, 435)
    }

    @Test
    fun `Kan kun jobbe deltid og ikke i hele Norge, ikke villig til å ta alle jobber eller gå ned i lønn`() {
        assertEquals(null, søknadprosess.resultat())

        søknadprosess.boolsk(`kan jobbe heltid`).besvar(false)
        assertEquals(null, søknadprosess.resultat())

        søknadprosess.flervalg(`årsak til kun deltid`).besvar(
            Flervalg("faktum.kun-deltid-aarsak.svar.omsorg-baby", "faktum.kun-deltid-aarsak.svar.annen-situasjon")
        )
        assertEquals(null, søknadprosess.resultat())

        søknadprosess.tekst(`skriv kort om situasjonen din`).besvar(Tekst("Jeg er omringet av maur"))
        assertEquals(null, søknadprosess.resultat())

        søknadprosess.heltall(`antall timer deltid du kan jobbe`).besvar(30)
        assertEquals(null, søknadprosess.resultat())

        søknadprosess.boolsk(`kan du jobbe i hele Norge`).besvar(false)
        assertEquals(null, søknadprosess.resultat())

        søknadprosess.flervalg(`årsak kan ikke jobbe i hele Norge`).besvar(
            Flervalg("faktum.ikke-jobbe-hele-norge.svar.redusert-helse", "faktum.ikke-jobbe-hele-norge.svar.annen-situasjon")
        )
        assertEquals(null, søknadprosess.resultat())

        søknadprosess.tekst(`kort om hvorfor ikke jobbe hele norge`).besvar(Tekst("Jeg er redd for hai"))
        assertEquals(null, søknadprosess.resultat())

        søknadprosess.boolsk(`kan ta alle typer arbeid`).besvar(false)
        assertEquals(null, søknadprosess.resultat())

        søknadprosess.boolsk(`kan bytte yrke eller gå ned i lønn`).besvar(false)
        assertEquals(true, søknadprosess.resultat())

        assertFaktaErBesvart(
            søknadprosess.boolsk(`kan jobbe heltid`),
            søknadprosess.flervalg(`årsak til kun deltid`),
            søknadprosess.tekst(`skriv kort om situasjonen din`),
            søknadprosess.heltall(`antall timer deltid du kan jobbe`),
            søknadprosess.boolsk(`kan du jobbe i hele Norge`),
            søknadprosess.flervalg(`årsak kan ikke jobbe i hele Norge`),
            søknadprosess.tekst(`kort om hvorfor ikke jobbe hele norge`),
            søknadprosess.boolsk(`kan ta alle typer arbeid`),
            søknadprosess.boolsk(`kan bytte yrke eller gå ned i lønn`)
        )
    }

    @Test
    fun `Kan jobbe heltid i hele Norge, jobbe med hva som helst, og gå ned i lønn`() {
        søknadprosess.boolsk(`kan jobbe heltid`).besvar(true)
        assertEquals(null, søknadprosess.resultat())

        søknadprosess.boolsk(`kan du jobbe i hele Norge`).besvar(true)
        assertEquals(null, søknadprosess.resultat())

        søknadprosess.boolsk(`kan ta alle typer arbeid`).besvar(true)
        assertEquals(null, søknadprosess.resultat())

        søknadprosess.boolsk(`kan bytte yrke eller gå ned i lønn`).besvar(true)
        assertEquals(true, søknadprosess.resultat())

        assertFaktaErBesvart(
            søknadprosess.boolsk(`kan jobbe heltid`),
            søknadprosess.boolsk(`kan du jobbe i hele Norge`),
            søknadprosess.boolsk(`kan ta alle typer arbeid`),
            søknadprosess.boolsk(`kan bytte yrke eller gå ned i lønn`)
        )
    }

    @Test
    fun `Fakta om hvorfor deltid blir besvart`() {
        `Besvar alle fakta hvorfor deltid`()

        assertFaktaErBesvart(
            søknadprosess.boolsk(`kan jobbe heltid`),
            søknadprosess.flervalg(`årsak til kun deltid`),
            søknadprosess.tekst(`skriv kort om situasjonen din`),
            søknadprosess.heltall(`antall timer deltid du kan jobbe`)
        )
    }

    @Test
    fun `Fakta om deltid blir riktig invalidert`() {
        `Besvar alle fakta hvorfor deltid`()
        søknadprosess.boolsk(`kan jobbe heltid`).besvar(true)

        assertAvhengigeFaktaInvalideres(
            søknadprosess.flervalg(`årsak til kun deltid`),
            søknadprosess.tekst(`skriv kort om situasjonen din`),
            søknadprosess.heltall(`antall timer deltid du kan jobbe`)
        )

        `Besvar alle fakta hvorfor deltid`()
        søknadprosess.flervalg(`årsak til kun deltid`).besvar(Flervalg("faktum.kun-deltid-aarsak.svar.annen-situasjon"))

        assertAvhengigeFaktaInvalideres(
            søknadprosess.tekst(`skriv kort om situasjonen din`)
        )

        `Besvar alle fakta hvorfor deltid`()
        søknadprosess.tekst(`skriv kort om situasjonen din`).besvar(Tekst("Noe greier"))
    }

    @Test
    fun `Fakta om hvorfor ikke jobbe i hele Norge blir besvart`() {
        `Besvar alle fakta hvorfor ikke jobbe i hele Norge`()

        assertFaktaErBesvart(
            søknadprosess.boolsk(`kan du jobbe i hele Norge`),
            søknadprosess.flervalg(`årsak kan ikke jobbe i hele Norge`),
            søknadprosess.tekst(`kort om hvorfor ikke jobbe hele norge`)
        )
    }

    @Test
    fun `Fakta om hvorfor ikke jobbe i hele Norge blir riktig invalidert`() {
        `Besvar alle fakta hvorfor ikke jobbe i hele Norge`()

        søknadprosess.boolsk(`kan du jobbe i hele Norge`).besvar(true)

        assertAvhengigeFaktaInvalideres(
            søknadprosess.flervalg(`årsak kan ikke jobbe i hele Norge`),
            søknadprosess.tekst(`kort om hvorfor ikke jobbe hele norge`)
        )

        `Besvar alle fakta hvorfor ikke jobbe i hele Norge`()

        søknadprosess.flervalg(`årsak kan ikke jobbe i hele Norge`).besvar(
            Flervalg("faktum.ikke-jobbe-hele-norge.svar.har-fylt-60")
        )

        assertAvhengigeFaktaInvalideres(
            søknadprosess.tekst(`kort om hvorfor ikke jobbe hele norge`)
        )
    }

    @Test
    fun `Faktum kan ta alle typer arbeid blir besvart`() {
        søknadprosess.boolsk(`kan ta alle typer arbeid`).besvar(true)
        assertEquals(true, søknadprosess.boolsk(`kan ta alle typer arbeid`).erBesvart())
    }

    @Test
    fun `Faktum kan bytte yrke og gå ned i lønn blir besvart`() {
        søknadprosess.boolsk(`kan bytte yrke eller gå ned i lønn`).besvar(true)
        assertEquals(true, søknadprosess.boolsk(`kan bytte yrke eller gå ned i lønn`).erBesvart())
    }

    private fun `Besvar alle fakta hvorfor deltid`() {
        søknadprosess.boolsk(`kan jobbe heltid`).besvar(false)
        søknadprosess.flervalg(`årsak til kun deltid`).besvar(
            Flervalg("faktum.kun-deltid-aarsak.svar.annen-situasjon")
        )
        søknadprosess.tekst(`skriv kort om situasjonen din`).besvar(Tekst("Jeg er omringet av maur"))
        søknadprosess.heltall(`antall timer deltid du kan jobbe`).besvar(20)
    }

    private fun `Besvar alle fakta hvorfor ikke jobbe i hele Norge`() {
        `Besvar alle fakta hvorfor deltid`()
        søknadprosess.boolsk(`kan du jobbe i hele Norge`).besvar(false)
        søknadprosess.flervalg(`årsak kan ikke jobbe i hele Norge`).besvar(
            Flervalg("faktum.ikke-jobbe-hele-norge.svar.redusert-helse", "faktum.ikke-jobbe-hele-norge.svar.annen-situasjon")
        )
        søknadprosess.tekst(`kort om hvorfor ikke jobbe hele norge`).besvar(Tekst("Jeg er redd for hai"))
    }

    @Test
    fun `Faktarekkefølge i seksjon`() {
        val faktaFraReellArbeidssøker = søknadprosess.nesteSeksjoner().first().joinToString(separator = ",") { it.id }
        assertEquals("1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29", faktaFraReellArbeidssøker)
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
