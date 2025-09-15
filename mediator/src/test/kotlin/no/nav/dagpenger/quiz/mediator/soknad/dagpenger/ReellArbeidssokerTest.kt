package no.nav.dagpenger.quiz.mediator.soknad.dagpenger

import no.nav.dagpenger.model.faktum.Fakta
import no.nav.dagpenger.model.faktum.Faktum
import no.nav.dagpenger.model.faktum.Flervalg
import no.nav.dagpenger.model.faktum.Tekst
import no.nav.dagpenger.model.seksjon.Prosess
import no.nav.dagpenger.quiz.mediator.helpers.testFaktaversjon
import no.nav.dagpenger.quiz.mediator.helpers.testSøknadprosess
import no.nav.dagpenger.quiz.mediator.soknad.dagpenger.ReellArbeidssoker.`antall timer deltid du kan jobbe`
import no.nav.dagpenger.quiz.mediator.soknad.dagpenger.ReellArbeidssoker.`kan bytte yrke eller gå ned i lønn`
import no.nav.dagpenger.quiz.mediator.soknad.dagpenger.ReellArbeidssoker.`kan du jobbe i hele Norge`
import no.nav.dagpenger.quiz.mediator.soknad.dagpenger.ReellArbeidssoker.`kan jobbe heltid`
import no.nav.dagpenger.quiz.mediator.soknad.dagpenger.ReellArbeidssoker.`kan ta alle typer arbeid`
import no.nav.dagpenger.quiz.mediator.soknad.dagpenger.ReellArbeidssoker.`kort om hvorfor ikke jobbe hele norge`
import no.nav.dagpenger.quiz.mediator.soknad.dagpenger.ReellArbeidssoker.`kort om hvorfor ikke jobbe heltid`
import no.nav.dagpenger.quiz.mediator.soknad.dagpenger.ReellArbeidssoker.`årsak kan ikke jobbe i hele Norge`
import no.nav.dagpenger.quiz.mediator.soknad.dagpenger.ReellArbeidssoker.`årsak til kun deltid`
import no.nav.dagpenger.quiz.mediator.soknad.verifiserFeltsammensetting
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class ReellArbeidssokerTest {
    private lateinit var fakta: Fakta
    private lateinit var prosess: Prosess

    @BeforeEach
    fun setup() {
        fakta = Fakta(testFaktaversjon(), *ReellArbeidssoker.fakta())
        prosess =
            fakta.testSøknadprosess(subsumsjon = ReellArbeidssoker.regeltre(fakta)) {
                ReellArbeidssoker.seksjon(this)
            }
    }

    @Test
    fun `Sjekk om faktasammensettingen har endret seg siden sist`() {
        ReellArbeidssoker.verifiserFeltsammensetting(13, 91)
    }

    @Test
    fun `Kan kun jobbe deltid og ikke i hele Norge, ikke villig til å ta alle jobber eller gå ned i lønn`() {
        assertEquals(null, prosess.resultat())

        prosess.boolsk(`kan jobbe heltid`).besvar(false)
        assertEquals(null, prosess.resultat())

        prosess.flervalg(`årsak til kun deltid`).besvar(
            Flervalg("faktum.kun-deltid-aarsak.svar.permittert", "faktum.kun-deltid-aarsak.svar.annen-situasjon"),
        )
        assertEquals(null, prosess.resultat())

        prosess.tekst(`kort om hvorfor ikke jobbe heltid`).besvar(Tekst("Jeg er omringet av maur"))
        assertEquals(null, prosess.resultat())

        prosess.desimaltall(`antall timer deltid du kan jobbe`).besvar(30.0)
        assertEquals(null, prosess.resultat())

        prosess.boolsk(`kan du jobbe i hele Norge`).besvar(false)
        assertEquals(null, prosess.resultat())

        prosess.flervalg(`årsak kan ikke jobbe i hele Norge`).besvar(
            Flervalg("faktum.ikke-jobbe-hele-norge.svar.redusert-helse", "faktum.ikke-jobbe-hele-norge.svar.permittert"),
        )
        assertEquals(null, prosess.resultat())

        prosess.tekst(`kort om hvorfor ikke jobbe hele norge`).besvar(Tekst("Jeg er redd for hai"))
        assertEquals(null, prosess.resultat())

        prosess.boolsk(`kan ta alle typer arbeid`).besvar(false)
        assertEquals(null, prosess.resultat())

        prosess.boolsk(`kan bytte yrke eller gå ned i lønn`).besvar(false)
        assertEquals(true, prosess.resultat())

        assertFaktaErBesvart(
            prosess.boolsk(`kan jobbe heltid`),
            prosess.flervalg(`årsak til kun deltid`),
            prosess.tekst(`kort om hvorfor ikke jobbe heltid`),
            prosess.desimaltall(`antall timer deltid du kan jobbe`),
            prosess.boolsk(`kan du jobbe i hele Norge`),
            prosess.flervalg(`årsak kan ikke jobbe i hele Norge`),
            prosess.tekst(`kort om hvorfor ikke jobbe hele norge`),
            prosess.boolsk(`kan ta alle typer arbeid`),
            prosess.boolsk(`kan bytte yrke eller gå ned i lønn`),
        )
    }

    @Test
    fun `Kan jobbe heltid i hele Norge, jobbe med hva som helst, og gå ned i lønn`() {
        prosess.boolsk(`kan jobbe heltid`).besvar(true)
        assertEquals(null, prosess.resultat())

        prosess.boolsk(`kan du jobbe i hele Norge`).besvar(true)
        assertEquals(null, prosess.resultat())

        prosess.boolsk(`kan ta alle typer arbeid`).besvar(true)
        assertEquals(null, prosess.resultat())

        prosess.boolsk(`kan bytte yrke eller gå ned i lønn`).besvar(true)
        assertEquals(true, prosess.resultat())

        assertFaktaErBesvart(
            prosess.boolsk(`kan jobbe heltid`),
            prosess.boolsk(`kan du jobbe i hele Norge`),
            prosess.boolsk(`kan ta alle typer arbeid`),
            prosess.boolsk(`kan bytte yrke eller gå ned i lønn`),
        )
    }

    @Test
    fun `Fakta om hvorfor deltid blir besvart`() {
        `Besvar alle fakta hvorfor deltid`()

        assertFaktaErBesvart(
            prosess.boolsk(`kan jobbe heltid`),
            prosess.flervalg(`årsak til kun deltid`),
            prosess.tekst(`kort om hvorfor ikke jobbe heltid`),
            prosess.desimaltall(`antall timer deltid du kan jobbe`),
        )
    }

    @Test
    fun `Fakta om deltid blir riktig invalidert`() {
        `Besvar alle fakta hvorfor deltid`()
        prosess.boolsk(`kan jobbe heltid`).besvar(true)

        assertAvhengigeFaktaInvalideres(
            prosess.flervalg(`årsak til kun deltid`),
            prosess.tekst(`kort om hvorfor ikke jobbe heltid`),
            prosess.desimaltall(`antall timer deltid du kan jobbe`),
        )

        `Besvar alle fakta hvorfor deltid`()
        prosess.flervalg(`årsak til kun deltid`).besvar(Flervalg("faktum.kun-deltid-aarsak.svar.annen-situasjon"))

        assertAvhengigeFaktaInvalideres(
            prosess.tekst(`kort om hvorfor ikke jobbe heltid`),
        )

        `Besvar alle fakta hvorfor deltid`()
        prosess.tekst(`kort om hvorfor ikke jobbe heltid`).besvar(Tekst("Noe greier"))
    }

    @Test
    fun `Fakta om hvorfor ikke jobbe i hele Norge blir besvart`() {
        `Besvar alle fakta hvorfor ikke jobbe i hele Norge`()

        assertFaktaErBesvart(
            prosess.boolsk(`kan du jobbe i hele Norge`),
            prosess.flervalg(`årsak kan ikke jobbe i hele Norge`),
            prosess.tekst(`kort om hvorfor ikke jobbe hele norge`),
        )
    }

    @Test
    fun `Fakta om hvorfor ikke jobbe i hele Norge blir riktig invalidert`() {
        `Besvar alle fakta hvorfor ikke jobbe i hele Norge`()

        prosess.boolsk(`kan du jobbe i hele Norge`).besvar(true)

        assertAvhengigeFaktaInvalideres(
            prosess.flervalg(`årsak kan ikke jobbe i hele Norge`),
            prosess.tekst(`kort om hvorfor ikke jobbe hele norge`),
        )

        `Besvar alle fakta hvorfor ikke jobbe i hele Norge`()

        prosess.flervalg(`årsak kan ikke jobbe i hele Norge`).besvar(
            Flervalg("faktum.ikke-jobbe-hele-norge.svar.har-fylt-60"),
        )

        assertAvhengigeFaktaInvalideres(
            prosess.tekst(`kort om hvorfor ikke jobbe hele norge`),
        )
    }

    @Test
    fun `Faktum kan ta alle typer arbeid blir besvart`() {
        prosess.boolsk(`kan ta alle typer arbeid`).besvar(true)
        assertEquals(true, prosess.boolsk(`kan ta alle typer arbeid`).erBesvart())
    }

    @Test
    fun `Faktum kan bytte yrke og gå ned i lønn blir besvart`() {
        prosess.boolsk(`kan bytte yrke eller gå ned i lønn`).besvar(true)
        assertEquals(true, prosess.boolsk(`kan bytte yrke eller gå ned i lønn`).erBesvart())
    }

    private fun `Besvar alle fakta hvorfor deltid`() {
        prosess.boolsk(`kan jobbe heltid`).besvar(false)
        prosess.flervalg(`årsak til kun deltid`).besvar(
            Flervalg("faktum.kun-deltid-aarsak.svar.annen-situasjon"),
        )
        prosess.tekst(`kort om hvorfor ikke jobbe heltid`).besvar(Tekst("Jeg er omringet av maur"))
        prosess.desimaltall(`antall timer deltid du kan jobbe`).besvar(20.5)
    }

    private fun `Besvar alle fakta hvorfor ikke jobbe i hele Norge`() {
        `Besvar alle fakta hvorfor deltid`()
        prosess.boolsk(`kan du jobbe i hele Norge`).besvar(false)
        prosess.flervalg(`årsak kan ikke jobbe i hele Norge`).besvar(
            Flervalg("faktum.ikke-jobbe-hele-norge.svar.redusert-helse", "faktum.ikke-jobbe-hele-norge.svar.annen-situasjon"),
        )
        prosess.tekst(`kort om hvorfor ikke jobbe hele norge`).besvar(Tekst("Jeg er redd for hai"))
    }

    @Test
    fun `Faktarekkefølge i seksjon`() {
        val faktaFraReellArbeidssøker = prosess.nesteSeksjoner().first().joinToString(separator = ",") { it.id }
        assertEquals("1,2,3,4,5,6,7,8,9,10,11,12", faktaFraReellArbeidssøker)
    }

    private fun assertFaktaErBesvart(vararg fakta: Faktum<*>) {
        fakta.forEach { faktum ->
            assertEquals(true, faktum.erBesvart(), "Faktum ${faktum.navn} skal være besvart")
        }
    }

    private fun assertAvhengigeFaktaInvalideres(vararg fakta: Faktum<*>) {
        fakta.forEach { faktum ->
            assertEquals(false, faktum.erBesvart(), "Faktum ${faktum.navn} skal være invalidert")
        }
    }
}
