package no.nav.dagpenger.quiz.mediator.soknad.dagpenger

import no.nav.dagpenger.model.faktum.Fakta
import no.nav.dagpenger.model.faktum.Faktum
import no.nav.dagpenger.model.faktum.Flervalg
import no.nav.dagpenger.model.faktum.HenvendelsesType
import no.nav.dagpenger.model.faktum.Tekst
import no.nav.dagpenger.model.seksjon.Utredningsprosess
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
    private val fakta = Fakta(HenvendelsesType(Prosess.Dagpenger, versjon = -1), *EgenNæring.fakta())
    private lateinit var utredningsprosess: Utredningsprosess

    @BeforeEach
    fun setup() {
        utredningsprosess = fakta.testSøknadprosess(
            EgenNæring.regeltre(fakta)
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
        utredningsprosess.boolsk(EgenNæring.`driver du egen næring`).besvar(true)
        utredningsprosess.generator(EgenNæring.`egen næring organisasjonsnummer liste`).besvar(2)
        assertEquals(null, utredningsprosess.resultat())

        utredningsprosess.heltall("${EgenNæring.`egen næring organisasjonsnummer`}.1").besvar(123)
        assertEquals(null, utredningsprosess.resultat())

        utredningsprosess.heltall("${EgenNæring.`egen næring organisasjonsnummer`}.2").besvar(456)
        assertEquals(null, utredningsprosess.resultat())

        utredningsprosess.desimaltall(EgenNæring.`egen næring arbeidstimer nå`).besvar(37.5)
        utredningsprosess.desimaltall(EgenNæring.`egen næring arbeidstimer før`).besvar(35.0)

        utredningsprosess.boolsk(EgenNæring.`driver du eget gårdsbruk`).besvar(false)
        assertEquals(true, utredningsprosess.resultat())
    }

    @Test
    fun `Har eget gårdsbruk`() {
        utredningsprosess.boolsk(EgenNæring.`driver du egen næring`).besvar(false)
        utredningsprosess.boolsk(EgenNæring.`driver du eget gårdsbruk`).besvar(true)
        assertEquals(null, utredningsprosess.resultat())

        utredningsprosess.heltall(EgenNæring.`eget gårdsbruk organisasjonsnummer`).besvar(123)
        utredningsprosess.flervalg(EgenNæring.`eget gårdsbruk type gårdsbruk`)
            .besvar(Flervalg("faktum.eget-gaardsbruk-type-gaardsbruk.svar.dyr"))
        utredningsprosess.flervalg(EgenNæring.`eget gårdsbruk hvem eier`).besvar(
            Flervalg(
                "faktum.eget-gaardsbruk-hvem-eier.svar.selv",
                "faktum.eget-gaardsbruk-hvem-eier.svar.ektefelle-samboer"
            )
        )
        utredningsprosess.desimaltall(EgenNæring.`eget gårdsbruk jeg andel inntekt`).besvar(50.0)
        utredningsprosess.desimaltall(EgenNæring.`eget gårdsbruk ektefelle samboer andel inntekt`).besvar(50.0)
        utredningsprosess.heltall(EgenNæring.`eget gårdsbruk arbeidsår for timer`).besvar(2020)
        utredningsprosess.desimaltall(EgenNæring.`eget gårdsbruk arbeidstimer år`).besvar(40.5)
        utredningsprosess.tekst(EgenNæring.`eget gårdsbruk arbeidstimer beregning`).besvar(Tekst("Brukte kalkulator"))
        assertEquals(true, utredningsprosess.resultat())
    }

    @Test
    fun `Verken egen næring eller eget gårdsbruk `() {
        utredningsprosess.boolsk(EgenNæring.`driver du egen næring`).besvar(false)
        utredningsprosess.boolsk(EgenNæring.`driver du eget gårdsbruk`).besvar(false)
        assertEquals(true, utredningsprosess.resultat())
    }

    @Test
    fun `Avhengigheter ved driver egen næring`() {
        utredningsprosess.boolsk(EgenNæring.`driver du egen næring`).besvar(true)
        utredningsprosess.generator(EgenNæring.`egen næring organisasjonsnummer liste`).besvar(1)
        utredningsprosess.heltall("${EgenNæring.`egen næring organisasjonsnummer`}.1").besvar(123)
        assertTrue(utredningsprosess.heltall("${EgenNæring.`egen næring organisasjonsnummer`}.1").erBesvart())

        utredningsprosess.boolsk(EgenNæring.`driver du egen næring`).besvar(false)
        // Nå blir generatoren ubesvart og alle templates tilbakestilles og fjernes.
        assertFalse(utredningsprosess.generator(EgenNæring.`egen næring organisasjonsnummer liste`).erBesvart())
        assertThrows<IllegalArgumentException> { utredningsprosess.heltall("${EgenNæring.`egen næring organisasjonsnummer`}.1") }
    }

    @Test
    fun `Avhengigheter ved eget gårdsbruk`() {
        val driverEgetGårdsbruk = utredningsprosess.boolsk(EgenNæring.`driver du eget gårdsbruk`)
        val gårdsbrukOrgnummer = utredningsprosess.heltall(EgenNæring.`eget gårdsbruk organisasjonsnummer`)
        val typeGårdsbruk = utredningsprosess.flervalg(EgenNæring.`eget gårdsbruk type gårdsbruk`)
        val eier = utredningsprosess.flervalg(EgenNæring.`eget gårdsbruk hvem eier`)
        val andelInntektSelv = utredningsprosess.desimaltall(EgenNæring.`eget gårdsbruk jeg andel inntekt`)
        val andelInntektSamboerEktefelle =
            utredningsprosess.desimaltall(EgenNæring.`eget gårdsbruk ektefelle samboer andel inntekt`)
        val årForArbeidstimer = utredningsprosess.heltall(EgenNæring.`eget gårdsbruk arbeidsår for timer`)
        val arbeidstimer = utredningsprosess.desimaltall(EgenNæring.`eget gårdsbruk arbeidstimer år`)
        val hvordanBeregnet = utredningsprosess.tekst(EgenNæring.`eget gårdsbruk arbeidstimer beregning`)

        driverEgetGårdsbruk.besvar(true)
        gårdsbrukOrgnummer.besvar(123)
        typeGårdsbruk.besvar(Flervalg("faktum.eget-gaardsbruk-type-gaardsbruk.svar.dyr"))
        eier.besvar(
            Flervalg(
                "faktum.eget-gaardsbruk-hvem-eier.svar.selv",
                "faktum.eget-gaardsbruk-hvem-eier.svar.ektefelle-samboer"
            )
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
        val faktaFraEgenNæring = utredningsprosess.nesteSeksjoner().first().joinToString(separator = ",") { it.id }
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
