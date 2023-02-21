package no.nav.dagpenger.quiz.mediator.soknad.dagpenger

import no.nav.dagpenger.model.faktum.Fakta
import no.nav.dagpenger.model.faktum.Faktaversjon
import no.nav.dagpenger.model.faktum.Faktum
import no.nav.dagpenger.model.faktum.Flervalg
import no.nav.dagpenger.model.faktum.Tekst
import no.nav.dagpenger.model.seksjon.Prosess
import no.nav.dagpenger.quiz.mediator.helpers.testSøknadprosess
import no.nav.dagpenger.quiz.mediator.soknad.Prosessfakta
import no.nav.dagpenger.quiz.mediator.soknad.verifiserFeltsammensetting
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class EgenNæringTest {
    private val fakta = Fakta(Faktaversjon(Prosessfakta.Dagpenger, versjon = -1), *EgenNæring.fakta())
    private lateinit var prosess: Prosess

    @BeforeEach
    fun setup() {
        prosess = fakta.testSøknadprosess(
            EgenNæring.regeltre(fakta),
        ) {
            EgenNæring.seksjon(this)
        }
    }

    @Test
    fun `Sjekk om faktasammensettingen har endret seg siden sist`() {
        EgenNæring.verifiserFeltsammensetting(15, 45120)
    }

    @Test
    fun `Driver egen næring`() {
        prosess.boolsk(EgenNæring.`driver du egen næring`).besvar(true)
        prosess.generator(EgenNæring.`egen næring organisasjonsnummer liste`).besvar(2)
        assertEquals(null, prosess.resultat())

        prosess.heltall("${EgenNæring.`egen næring organisasjonsnummer`}.1").besvar(123)
        assertEquals(null, prosess.resultat())

        prosess.heltall("${EgenNæring.`egen næring organisasjonsnummer`}.2").besvar(456)
        assertEquals(null, prosess.resultat())

        prosess.desimaltall(EgenNæring.`egen næring arbeidstimer nå`).besvar(37.5)
        prosess.desimaltall(EgenNæring.`egen næring arbeidstimer før`).besvar(35.0)

        prosess.boolsk(EgenNæring.`driver du eget gårdsbruk`).besvar(false)
        assertEquals(true, prosess.resultat())
    }

    @Test
    fun `Har eget gårdsbruk`() {
        prosess.boolsk(EgenNæring.`driver du egen næring`).besvar(false)
        prosess.boolsk(EgenNæring.`driver du eget gårdsbruk`).besvar(true)
        assertEquals(null, prosess.resultat())

        prosess.heltall(EgenNæring.`eget gårdsbruk organisasjonsnummer`).besvar(123)
        prosess.flervalg(EgenNæring.`eget gårdsbruk type gårdsbruk`)
            .besvar(Flervalg("faktum.eget-gaardsbruk-type-gaardsbruk.svar.dyr"))
        prosess.flervalg(EgenNæring.`eget gårdsbruk hvem eier`).besvar(
            Flervalg(
                "faktum.eget-gaardsbruk-hvem-eier.svar.selv",
                "faktum.eget-gaardsbruk-hvem-eier.svar.ektefelle-samboer",
            ),
        )
        prosess.desimaltall(EgenNæring.`eget gårdsbruk jeg andel inntekt`).besvar(50.0)
        prosess.desimaltall(EgenNæring.`eget gårdsbruk ektefelle samboer andel inntekt`).besvar(50.0)
        prosess.heltall(EgenNæring.`eget gårdsbruk arbeidsår for timer`).besvar(2020)
        prosess.desimaltall(EgenNæring.`eget gårdsbruk arbeidstimer år`).besvar(40.5)
        prosess.tekst(EgenNæring.`eget gårdsbruk arbeidstimer beregning`).besvar(Tekst("Brukte kalkulator"))
        assertEquals(true, prosess.resultat())
    }

    @Test
    fun `Verken egen næring eller eget gårdsbruk `() {
        prosess.boolsk(EgenNæring.`driver du egen næring`).besvar(false)
        prosess.boolsk(EgenNæring.`driver du eget gårdsbruk`).besvar(false)
        assertEquals(true, prosess.resultat())
    }

    @Test
    fun `Avhengigheter ved driver egen næring`() {
        prosess.boolsk(EgenNæring.`driver du egen næring`).besvar(true)
        prosess.generator(EgenNæring.`egen næring organisasjonsnummer liste`).besvar(1)
        prosess.heltall("${EgenNæring.`egen næring organisasjonsnummer`}.1").besvar(123)
        assertTrue(prosess.heltall("${EgenNæring.`egen næring organisasjonsnummer`}.1").erBesvart())

        prosess.boolsk(EgenNæring.`driver du egen næring`).besvar(false)
        // Nå blir generatoren ubesvart og alle templates tilbakestilles og fjernes.
        assertFalse(prosess.generator(EgenNæring.`egen næring organisasjonsnummer liste`).erBesvart())
        assertThrows<IllegalArgumentException> { prosess.heltall("${EgenNæring.`egen næring organisasjonsnummer`}.1") }
    }

    @Test
    fun `Avhengigheter ved eget gårdsbruk`() {
        val driverEgetGårdsbruk = prosess.boolsk(EgenNæring.`driver du eget gårdsbruk`)
        val gårdsbrukOrgnummer = prosess.heltall(EgenNæring.`eget gårdsbruk organisasjonsnummer`)
        val typeGårdsbruk = prosess.flervalg(EgenNæring.`eget gårdsbruk type gårdsbruk`)
        val eier = prosess.flervalg(EgenNæring.`eget gårdsbruk hvem eier`)
        val andelInntektSelv = prosess.desimaltall(EgenNæring.`eget gårdsbruk jeg andel inntekt`)
        val andelInntektSamboerEktefelle =
            prosess.desimaltall(EgenNæring.`eget gårdsbruk ektefelle samboer andel inntekt`)
        val årForArbeidstimer = prosess.heltall(EgenNæring.`eget gårdsbruk arbeidsår for timer`)
        val arbeidstimer = prosess.desimaltall(EgenNæring.`eget gårdsbruk arbeidstimer år`)
        val hvordanBeregnet = prosess.tekst(EgenNæring.`eget gårdsbruk arbeidstimer beregning`)

        driverEgetGårdsbruk.besvar(true)
        gårdsbrukOrgnummer.besvar(123)
        typeGårdsbruk.besvar(Flervalg("faktum.eget-gaardsbruk-type-gaardsbruk.svar.dyr"))
        eier.besvar(
            Flervalg(
                "faktum.eget-gaardsbruk-hvem-eier.svar.selv",
                "faktum.eget-gaardsbruk-hvem-eier.svar.ektefelle-samboer",
            ),
        )
        andelInntektSelv.besvar(60.0)
        andelInntektSamboerEktefelle.besvar(40.0)
        årForArbeidstimer.besvar(2021)
        arbeidstimer.besvar(40.5)
        hvordanBeregnet.besvar(Tekst("forklaring"))

        assertErBesvarte(gårdsbrukOrgnummer, typeGårdsbruk, eier, årForArbeidstimer, arbeidstimer, hvordanBeregnet)

        driverEgetGårdsbruk.besvar(false)
        assertErUbesvarte(gårdsbrukOrgnummer, typeGårdsbruk, eier, andelInntektSelv, andelInntektSamboerEktefelle, årForArbeidstimer, arbeidstimer, hvordanBeregnet)
    }

    @Test
    fun `Faktarekkefølge i seksjon`() {
        val faktaFraEgenNæring = prosess.nesteSeksjoner().first().joinToString(separator = ",") { it.id }
        assertEquals("3001,3002,3003,3005,3004,3006,3007,3008,3009,3010,3011,3012,3014,3013,3015", faktaFraEgenNæring)
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
