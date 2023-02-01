package no.nav.dagpenger.quiz.mediator.soknad.dagpenger

import no.nav.dagpenger.model.faktum.Fakta
import no.nav.dagpenger.model.faktum.Faktum
import no.nav.dagpenger.model.faktum.Flervalg
import no.nav.dagpenger.model.faktum.FaktaVersjon
import no.nav.dagpenger.model.faktum.Tekst
import no.nav.dagpenger.model.seksjon.Utredningsprosess
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

    private lateinit var fakta: Fakta
    private lateinit var utredningsprosess: Utredningsprosess

    @BeforeEach
    fun setup() {
        fakta = Fakta(FaktaVersjon(Prosess.Dagpenger, -1), *ReellArbeidssoker.fakta())
        utredningsprosess = fakta.testSøknadprosess(ReellArbeidssoker.regeltre(fakta)) {
            ReellArbeidssoker.seksjon(this)
        }
    }

    @Test
    fun `Sjekk om faktasammensettingen har endret seg siden sist`() {
        ReellArbeidssoker.verifiserFeltsammensetting(13, 91)
    }

    @Test
    fun `Kan kun jobbe deltid og ikke i hele Norge, ikke villig til å ta alle jobber eller gå ned i lønn`() {
        assertEquals(null, utredningsprosess.resultat())

        utredningsprosess.boolsk(`kan jobbe heltid`).besvar(false)
        assertEquals(null, utredningsprosess.resultat())

        utredningsprosess.flervalg(`årsak til kun deltid`).besvar(
            Flervalg("faktum.kun-deltid-aarsak.svar.omsorg-baby", "faktum.kun-deltid-aarsak.svar.annen-situasjon")
        )
        assertEquals(null, utredningsprosess.resultat())

        utredningsprosess.tekst(`skriv kort om situasjonen din`).besvar(Tekst("Jeg er omringet av maur"))
        assertEquals(null, utredningsprosess.resultat())

        utredningsprosess.desimaltall(`antall timer deltid du kan jobbe`).besvar(30.0)
        assertEquals(null, utredningsprosess.resultat())

        utredningsprosess.boolsk(`kan du jobbe i hele Norge`).besvar(false)
        assertEquals(null, utredningsprosess.resultat())

        utredningsprosess.flervalg(`årsak kan ikke jobbe i hele Norge`).besvar(
            Flervalg("faktum.ikke-jobbe-hele-norge.svar.redusert-helse", "faktum.ikke-jobbe-hele-norge.svar.annen-situasjon")
        )
        assertEquals(null, utredningsprosess.resultat())

        utredningsprosess.tekst(`kort om hvorfor ikke jobbe hele norge`).besvar(Tekst("Jeg er redd for hai"))
        assertEquals(null, utredningsprosess.resultat())

        utredningsprosess.boolsk(`kan ta alle typer arbeid`).besvar(false)
        assertEquals(null, utredningsprosess.resultat())

        utredningsprosess.boolsk(`kan bytte yrke eller gå ned i lønn`).besvar(false)
        assertEquals(true, utredningsprosess.resultat())

        assertFaktaErBesvart(
            utredningsprosess.boolsk(`kan jobbe heltid`),
            utredningsprosess.flervalg(`årsak til kun deltid`),
            utredningsprosess.tekst(`skriv kort om situasjonen din`),
            utredningsprosess.desimaltall(`antall timer deltid du kan jobbe`),
            utredningsprosess.boolsk(`kan du jobbe i hele Norge`),
            utredningsprosess.flervalg(`årsak kan ikke jobbe i hele Norge`),
            utredningsprosess.tekst(`kort om hvorfor ikke jobbe hele norge`),
            utredningsprosess.boolsk(`kan ta alle typer arbeid`),
            utredningsprosess.boolsk(`kan bytte yrke eller gå ned i lønn`)
        )
    }

    @Test
    fun `Kan jobbe heltid i hele Norge, jobbe med hva som helst, og gå ned i lønn`() {
        utredningsprosess.boolsk(`kan jobbe heltid`).besvar(true)
        assertEquals(null, utredningsprosess.resultat())

        utredningsprosess.boolsk(`kan du jobbe i hele Norge`).besvar(true)
        assertEquals(null, utredningsprosess.resultat())

        utredningsprosess.boolsk(`kan ta alle typer arbeid`).besvar(true)
        assertEquals(null, utredningsprosess.resultat())

        utredningsprosess.boolsk(`kan bytte yrke eller gå ned i lønn`).besvar(true)
        assertEquals(true, utredningsprosess.resultat())

        assertFaktaErBesvart(
            utredningsprosess.boolsk(`kan jobbe heltid`),
            utredningsprosess.boolsk(`kan du jobbe i hele Norge`),
            utredningsprosess.boolsk(`kan ta alle typer arbeid`),
            utredningsprosess.boolsk(`kan bytte yrke eller gå ned i lønn`)
        )
    }

    @Test
    fun `Fakta om hvorfor deltid blir besvart`() {
        `Besvar alle fakta hvorfor deltid`()

        assertFaktaErBesvart(
            utredningsprosess.boolsk(`kan jobbe heltid`),
            utredningsprosess.flervalg(`årsak til kun deltid`),
            utredningsprosess.tekst(`skriv kort om situasjonen din`),
            utredningsprosess.desimaltall(`antall timer deltid du kan jobbe`)
        )
    }

    @Test
    fun `Fakta om deltid blir riktig invalidert`() {
        `Besvar alle fakta hvorfor deltid`()
        utredningsprosess.boolsk(`kan jobbe heltid`).besvar(true)

        assertAvhengigeFaktaInvalideres(
            utredningsprosess.flervalg(`årsak til kun deltid`),
            utredningsprosess.tekst(`skriv kort om situasjonen din`),
            utredningsprosess.desimaltall(`antall timer deltid du kan jobbe`)
        )

        `Besvar alle fakta hvorfor deltid`()
        utredningsprosess.flervalg(`årsak til kun deltid`).besvar(Flervalg("faktum.kun-deltid-aarsak.svar.annen-situasjon"))

        assertAvhengigeFaktaInvalideres(
            utredningsprosess.tekst(`skriv kort om situasjonen din`)
        )

        `Besvar alle fakta hvorfor deltid`()
        utredningsprosess.tekst(`skriv kort om situasjonen din`).besvar(Tekst("Noe greier"))
    }

    @Test
    fun `Fakta om hvorfor ikke jobbe i hele Norge blir besvart`() {
        `Besvar alle fakta hvorfor ikke jobbe i hele Norge`()

        assertFaktaErBesvart(
            utredningsprosess.boolsk(`kan du jobbe i hele Norge`),
            utredningsprosess.flervalg(`årsak kan ikke jobbe i hele Norge`),
            utredningsprosess.tekst(`kort om hvorfor ikke jobbe hele norge`)
        )
    }

    @Test
    fun `Fakta om hvorfor ikke jobbe i hele Norge blir riktig invalidert`() {
        `Besvar alle fakta hvorfor ikke jobbe i hele Norge`()

        utredningsprosess.boolsk(`kan du jobbe i hele Norge`).besvar(true)

        assertAvhengigeFaktaInvalideres(
            utredningsprosess.flervalg(`årsak kan ikke jobbe i hele Norge`),
            utredningsprosess.tekst(`kort om hvorfor ikke jobbe hele norge`)
        )

        `Besvar alle fakta hvorfor ikke jobbe i hele Norge`()

        utredningsprosess.flervalg(`årsak kan ikke jobbe i hele Norge`).besvar(
            Flervalg("faktum.ikke-jobbe-hele-norge.svar.har-fylt-60")
        )

        assertAvhengigeFaktaInvalideres(
            utredningsprosess.tekst(`kort om hvorfor ikke jobbe hele norge`)
        )
    }

    @Test
    fun `Faktum kan ta alle typer arbeid blir besvart`() {
        utredningsprosess.boolsk(`kan ta alle typer arbeid`).besvar(true)
        assertEquals(true, utredningsprosess.boolsk(`kan ta alle typer arbeid`).erBesvart())
    }

    @Test
    fun `Faktum kan bytte yrke og gå ned i lønn blir besvart`() {
        utredningsprosess.boolsk(`kan bytte yrke eller gå ned i lønn`).besvar(true)
        assertEquals(true, utredningsprosess.boolsk(`kan bytte yrke eller gå ned i lønn`).erBesvart())
    }

    private fun `Besvar alle fakta hvorfor deltid`() {
        utredningsprosess.boolsk(`kan jobbe heltid`).besvar(false)
        utredningsprosess.flervalg(`årsak til kun deltid`).besvar(
            Flervalg("faktum.kun-deltid-aarsak.svar.annen-situasjon")
        )
        utredningsprosess.tekst(`skriv kort om situasjonen din`).besvar(Tekst("Jeg er omringet av maur"))
        utredningsprosess.desimaltall(`antall timer deltid du kan jobbe`).besvar(20.5)
    }

    private fun `Besvar alle fakta hvorfor ikke jobbe i hele Norge`() {
        `Besvar alle fakta hvorfor deltid`()
        utredningsprosess.boolsk(`kan du jobbe i hele Norge`).besvar(false)
        utredningsprosess.flervalg(`årsak kan ikke jobbe i hele Norge`).besvar(
            Flervalg("faktum.ikke-jobbe-hele-norge.svar.redusert-helse", "faktum.ikke-jobbe-hele-norge.svar.annen-situasjon")
        )
        utredningsprosess.tekst(`kort om hvorfor ikke jobbe hele norge`).besvar(Tekst("Jeg er redd for hai"))
    }

    @Test
    fun `Faktarekkefølge i seksjon`() {
        val faktaFraReellArbeidssøker = utredningsprosess.nesteSeksjoner().first().joinToString(separator = ",") { it.id }
        assertEquals("1,2,3,4,5,6,7,8,9,10,11,12", faktaFraReellArbeidssøker)
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
