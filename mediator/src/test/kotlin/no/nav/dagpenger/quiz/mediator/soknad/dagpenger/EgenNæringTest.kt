package no.nav.dagpenger.quiz.mediator.soknad.dagpenger

import no.nav.dagpenger.model.faktum.Faktum
import no.nav.dagpenger.model.faktum.Flervalg
import no.nav.dagpenger.model.faktum.Prosessversjon
import no.nav.dagpenger.model.faktum.Fakta
import no.nav.dagpenger.model.faktum.Tekst
import no.nav.dagpenger.model.seksjon.Faktagrupper
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
    private val fakta = Fakta(Prosessversjon(Prosess.Dagpenger, versjon = -1), *EgenNæring.fakta())
    private lateinit var faktagrupper: Faktagrupper

    @BeforeEach
    fun setup() {
        faktagrupper = fakta.testSøknadprosess(
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
        faktagrupper.boolsk(EgenNæring.`driver du egen næring`).besvar(true)
        faktagrupper.generator(EgenNæring.`egen næring organisasjonsnummer liste`).besvar(2)
        assertEquals(null, faktagrupper.resultat())

        faktagrupper.heltall("${EgenNæring.`egen næring organisasjonsnummer`}.1").besvar(123)
        assertEquals(null, faktagrupper.resultat())

        faktagrupper.heltall("${EgenNæring.`egen næring organisasjonsnummer`}.2").besvar(456)
        assertEquals(null, faktagrupper.resultat())

        faktagrupper.desimaltall(EgenNæring.`egen næring arbeidstimer nå`).besvar(37.5)
        faktagrupper.desimaltall(EgenNæring.`egen næring arbeidstimer før`).besvar(35.0)

        faktagrupper.boolsk(EgenNæring.`driver du eget gårdsbruk`).besvar(false)
        assertEquals(true, faktagrupper.resultat())
    }

    @Test
    fun `Har eget gårdsbruk`() {
        faktagrupper.boolsk(EgenNæring.`driver du egen næring`).besvar(false)
        faktagrupper.boolsk(EgenNæring.`driver du eget gårdsbruk`).besvar(true)
        assertEquals(null, faktagrupper.resultat())

        faktagrupper.heltall(EgenNæring.`eget gårdsbruk organisasjonsnummer`).besvar(123)
        faktagrupper.flervalg(EgenNæring.`eget gårdsbruk type gårdsbruk`)
            .besvar(Flervalg("faktum.eget-gaardsbruk-type-gaardsbruk.svar.dyr"))
        faktagrupper.flervalg(EgenNæring.`eget gårdsbruk hvem eier`).besvar(
            Flervalg(
                "faktum.eget-gaardsbruk-hvem-eier.svar.selv",
                "faktum.eget-gaardsbruk-hvem-eier.svar.ektefelle-samboer"
            )
        )
        faktagrupper.desimaltall(EgenNæring.`eget gårdsbruk jeg andel inntekt`).besvar(50.0)
        faktagrupper.desimaltall(EgenNæring.`eget gårdsbruk ektefelle samboer andel inntekt`).besvar(50.0)
        faktagrupper.heltall(EgenNæring.`eget gårdsbruk arbeidsår for timer`).besvar(2020)
        faktagrupper.desimaltall(EgenNæring.`eget gårdsbruk arbeidstimer år`).besvar(40.5)
        faktagrupper.tekst(EgenNæring.`eget gårdsbruk arbeidstimer beregning`).besvar(Tekst("Brukte kalkulator"))
        assertEquals(true, faktagrupper.resultat())
    }

    @Test
    fun `Verken egen næring eller eget gårdsbruk `() {
        faktagrupper.boolsk(EgenNæring.`driver du egen næring`).besvar(false)
        faktagrupper.boolsk(EgenNæring.`driver du eget gårdsbruk`).besvar(false)
        assertEquals(true, faktagrupper.resultat())
    }

    @Test
    fun `Avhengigheter ved driver egen næring`() {
        faktagrupper.boolsk(EgenNæring.`driver du egen næring`).besvar(true)
        faktagrupper.generator(EgenNæring.`egen næring organisasjonsnummer liste`).besvar(1)
        faktagrupper.heltall("${EgenNæring.`egen næring organisasjonsnummer`}.1").besvar(123)
        assertTrue(faktagrupper.heltall("${EgenNæring.`egen næring organisasjonsnummer`}.1").erBesvart())

        faktagrupper.boolsk(EgenNæring.`driver du egen næring`).besvar(false)
        // Nå blir generatoren ubesvart og alle templates tilbakestilles og fjernes.
        assertFalse(faktagrupper.generator(EgenNæring.`egen næring organisasjonsnummer liste`).erBesvart())
        assertThrows<IllegalArgumentException> { faktagrupper.heltall("${EgenNæring.`egen næring organisasjonsnummer`}.1") }
    }

    @Test
    fun `Avhengigheter ved eget gårdsbruk`() {
        val driverEgetGårdsbruk = faktagrupper.boolsk(EgenNæring.`driver du eget gårdsbruk`)
        val gårdsbrukOrgnummer = faktagrupper.heltall(EgenNæring.`eget gårdsbruk organisasjonsnummer`)
        val typeGårdsbruk = faktagrupper.flervalg(EgenNæring.`eget gårdsbruk type gårdsbruk`)
        val eier = faktagrupper.flervalg(EgenNæring.`eget gårdsbruk hvem eier`)
        val andelInntektSelv = faktagrupper.desimaltall(EgenNæring.`eget gårdsbruk jeg andel inntekt`)
        val andelInntektSamboerEktefelle =
            faktagrupper.desimaltall(EgenNæring.`eget gårdsbruk ektefelle samboer andel inntekt`)
        val årForArbeidstimer = faktagrupper.heltall(EgenNæring.`eget gårdsbruk arbeidsår for timer`)
        val arbeidstimer = faktagrupper.desimaltall(EgenNæring.`eget gårdsbruk arbeidstimer år`)
        val hvordanBeregnet = faktagrupper.tekst(EgenNæring.`eget gårdsbruk arbeidstimer beregning`)

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
        val faktaFraEgenNæring = faktagrupper.nesteSeksjoner().first().joinToString(separator = ",") { it.id }
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
