package no.nav.dagpenger.quiz.mediator.soknad.dagpenger

import no.nav.dagpenger.model.faktum.Faktum
import no.nav.dagpenger.model.faktum.Flervalg
import no.nav.dagpenger.model.faktum.Prosessversjon
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.faktum.Tekst
import no.nav.dagpenger.model.seksjon.Søknadprosess
import no.nav.dagpenger.quiz.mediator.helpers.testSøknadprosess
import no.nav.dagpenger.quiz.mediator.soknad.Prosess
import no.nav.dagpenger.quiz.mediator.soknad.verifiserFeltsammensetting
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class EgenNæringTest {
    private val søknad = Søknad(Prosessversjon(Prosess.Dagpenger, versjon = -1), *EgenNæring.fakta())
    private lateinit var søknadprosess: Søknadprosess

    @BeforeEach
    fun setup() {
        søknadprosess = søknad.testSøknadprosess(
            EgenNæring.regeltre(søknad)
        ) {
            EgenNæring.seksjon(this)
        }
    }

    @Test
    fun `Sjekk om faktasammensettingen har endret seg siden sist`() {
        EgenNæring.verifiserFeltsammensetting(18, 54171)
    }

    @Test
    fun `Driver egen næring`() {
        søknadprosess.boolsk(EgenNæring.`driver du egen næring`).besvar(true)
        søknadprosess.generator(EgenNæring.`egen næring organisasjonsnummer liste`).besvar(2)
        assertEquals(null, søknadprosess.resultat())

        søknadprosess.heltall("${EgenNæring.`egen næring organisasjonsnummer`}.1").besvar(123)
        assertEquals(null, søknadprosess.resultat())

        søknadprosess.heltall("${EgenNæring.`egen næring organisasjonsnummer`}.2").besvar(456)
        assertEquals(null, søknadprosess.resultat())

        søknadprosess.desimaltall(EgenNæring.`egen næring arbeidstimer nå`).besvar(37.5)
        søknadprosess.desimaltall(EgenNæring.`egen næring arbeidstimer før`).besvar(35.0)

        søknadprosess.boolsk(EgenNæring.`driver du eget gårdsbruk`).besvar(false)
        assertEquals(true, søknadprosess.resultat())
    }

    @Test
    fun `Har eget gårdsbruk`() {
        søknadprosess.boolsk(EgenNæring.`driver du egen næring`).besvar(false)
        søknadprosess.boolsk(EgenNæring.`driver du eget gårdsbruk`).besvar(true)
        assertEquals(null, søknadprosess.resultat())

        søknadprosess.heltall(EgenNæring.`eget gårdsbruk organisasjonsnummer`).besvar(123)
        søknadprosess.flervalg(EgenNæring.`eget gårdsbruk type gårdsbruk`)
            .besvar(Flervalg("faktum.eget-gaardsbruk-type-gaardsbruk.svar.dyr"))
        søknadprosess.flervalg(EgenNæring.`eget gårdsbruk hvem eier`).besvar(
            Flervalg(
                "faktum.eget-gaardsbruk-hvem-eier.svar.selv",
                "faktum.eget-gaardsbruk-hvem-eier.svar.ektefelle-samboer"
            )
        )
        søknadprosess.desimaltall(EgenNæring.`eget gårdsbruk jeg andel inntekt`).besvar(50.0)
        søknadprosess.desimaltall(EgenNæring.`eget gårdsbruk ektefelle samboer andel inntekt`).besvar(50.0)
        søknadprosess.heltall(EgenNæring.`eget gårdsbruk arbeidsår for timer`).besvar(2020)
        søknadprosess.desimaltall(EgenNæring.`eget gårdsbruk arbeidstimer år`).besvar(40.5)
        søknadprosess.tekst(EgenNæring.`eget gårdsbruk arbeidstimer beregning`).besvar(Tekst("Brukte kalkulator"))
        assertEquals(true, søknadprosess.resultat())
    }

    @Test
    fun `Verken egen næring eller eget gårdsbruk `() {
        søknadprosess.boolsk(EgenNæring.`driver du egen næring`).besvar(false)
        søknadprosess.boolsk(EgenNæring.`driver du eget gårdsbruk`).besvar(false)
        assertEquals(true, søknadprosess.resultat())
    }

    @Test
    fun `Avhengigheter ved driver egen næring`() {
        søknadprosess.boolsk(EgenNæring.`driver du egen næring`).besvar(true)
        søknadprosess.generator(EgenNæring.`egen næring organisasjonsnummer liste`).besvar(1)
        søknadprosess.heltall("${EgenNæring.`egen næring organisasjonsnummer`}.1").besvar(123)
        assertTrue(søknadprosess.heltall("${EgenNæring.`egen næring organisasjonsnummer`}.1").erBesvart())

        søknadprosess.boolsk(EgenNæring.`driver du egen næring`).besvar(false)
        // Nå blir generatoren ubesvart og alle templates tilbakestilles og fjernes.
        assertFalse(søknadprosess.generator(EgenNæring.`egen næring organisasjonsnummer liste`).erBesvart())
        assertThrows<IllegalArgumentException> { søknadprosess.heltall("${EgenNæring.`egen næring organisasjonsnummer`}.1") }
    }

    @Test
    fun `Avhengigheter ved eget gårdsbruk`() {
        val driverEgetGårdsbruk = søknadprosess.boolsk(EgenNæring.`driver du eget gårdsbruk`)
        val gårdsbrukOrgnummer = søknadprosess.heltall(EgenNæring.`eget gårdsbruk organisasjonsnummer`)
        val typeGårdsbruk = søknadprosess.flervalg(EgenNæring.`eget gårdsbruk type gårdsbruk`)
        val eier = søknadprosess.flervalg(EgenNæring.`eget gårdsbruk hvem eier`)
        val årForArbeidstimer = søknadprosess.heltall(EgenNæring.`eget gårdsbruk arbeidsår for timer`)
        val arbeidstimer = søknadprosess.desimaltall(EgenNæring.`eget gårdsbruk arbeidstimer år`)
        val hvordanBeregnet = søknadprosess.tekst(EgenNæring.`eget gårdsbruk arbeidstimer beregning`)

        driverEgetGårdsbruk.besvar(true)
        gårdsbrukOrgnummer.besvar(123)
        typeGårdsbruk.besvar(Flervalg("faktum.eget-gaardsbruk-type-gaardsbruk.svar.dyr"))
        eier.besvar(
            Flervalg(
                "faktum.eget-gaardsbruk-hvem-eier.svar.selv",
                "faktum.eget-gaardsbruk-hvem-eier.svar.ektefelle-samboer"
            )
        )
        årForArbeidstimer.besvar(2021)
        arbeidstimer.besvar(40.5)
        hvordanBeregnet.besvar(Tekst("forklaring"))

        assertErBesvarte(gårdsbrukOrgnummer, typeGårdsbruk, eier, årForArbeidstimer, arbeidstimer, hvordanBeregnet)

        driverEgetGårdsbruk.besvar(false)
        assertErUbesvarte(gårdsbrukOrgnummer, typeGårdsbruk, eier, årForArbeidstimer, arbeidstimer, hvordanBeregnet)
    }

    @Test
    fun `Faktarekkefølge i seksjon`() {
        val faktaFraEgenNæring = søknadprosess.nesteSeksjoner().first().joinToString(separator = ",") { it.id }
        assertEquals("3001,3002,3003,3005,3004,3006,3007,3008,3009,3010,3011,3012,3014,3013,3015,3016,3017", faktaFraEgenNæring)
    }

    @Test
    fun `Avhengigheter ved eier av gårdsbruket`() {
        val eier = søknadprosess.flervalg(EgenNæring.`eget gårdsbruk hvem eier`)
        val andelInntektSelv = søknadprosess.desimaltall(EgenNæring.`eget gårdsbruk jeg andel inntekt`)
        val andelInntektSamboerEktefelle =
            søknadprosess.desimaltall(EgenNæring.`eget gårdsbruk ektefelle samboer andel inntekt`)

        eier.besvar(
            Flervalg(
                "faktum.eget-gaardsbruk-hvem-eier.svar.selv",
                "faktum.eget-gaardsbruk-hvem-eier.svar.ektefelle-samboer"
            )
        )
        andelInntektSelv.besvar(60.0)
        andelInntektSamboerEktefelle.besvar(40.0)
        assertErBesvarte(andelInntektSelv, andelInntektSamboerEktefelle)

        eier.besvar(Flervalg("faktum.eget-gaardsbruk-hvem-eier.svar.andre"))
        assertErUbesvarte(andelInntektSelv, andelInntektSamboerEktefelle)
    }

    private fun assertErBesvarte(vararg fakta: Faktum<*>) =
        fakta.forEach { faktum ->
            assertTrue(faktum.erBesvart())
        }

    private fun assertErUbesvarte(vararg fakta: Faktum<*>) =
        fakta.forEach { faktum ->
            assertFalse(faktum.erBesvart())
        }
}
