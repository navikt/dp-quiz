package no.nav.dagpenger.quiz.mediator.soknad.dagpenger

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
import kotlin.test.assertEquals

internal class EgenNæringTest {

    private val søknad = Søknad(Prosessversjon(Prosess.Dagpenger, versjon = -1), *EgenNæring.fakta())
    private lateinit var søknadprosess: Søknadprosess

    @Test
    fun `Sjekk om faktasammensettingen har endret seg siden sist`() {
        EgenNæring.verifiserFeltsammensetting(15, 45120)
    }

    @BeforeEach
    fun setup() {
        søknadprosess = søknad.testSøknadprosess(
            EgenNæring.regeltre(søknad)
        )
    }

    @Test
    fun `Driver egen næring`() {
        søknadprosess.boolsk(EgenNæring.`driver du egen naering`).besvar(true)
        søknadprosess.generator(EgenNæring.`egen naering organisasjonsnummer liste`).besvar(2)
        assertEquals(null, søknadprosess.resultat())

        søknadprosess.heltall("${EgenNæring.`egen naering organisasjonsnummer`}.1").besvar(123)
        assertEquals(null, søknadprosess.resultat())

        søknadprosess.heltall("${EgenNæring.`egen naering organisasjonsnummer`}.2").besvar(456)
        assertEquals(null, søknadprosess.resultat())

        søknadprosess.desimaltall(EgenNæring.`egen naering arbeidstimer naa`).besvar(37.5)
        søknadprosess.desimaltall(EgenNæring.`egen naering arbeidstimer for`).besvar(35.0)

        søknadprosess.boolsk(EgenNæring.`driver du eget gaardsbruk`).besvar(false)
        assertEquals(true, søknadprosess.resultat())
    }

    @Test
    fun `Har eget gårdsbruk`() {
        søknadprosess.boolsk(EgenNæring.`driver du egen naering`).besvar(false)
        søknadprosess.boolsk(EgenNæring.`driver du eget gaardsbruk`).besvar(true)
        assertEquals(null, søknadprosess.resultat())

        søknadprosess.heltall(EgenNæring.`eget gaardsbruk organisasjonsnummer`).besvar(123)
        søknadprosess.flervalg(EgenNæring.`eget gaardsbruk type gaardsbruk`).besvar(Flervalg("faktum.eget-gaardsbruk-type-gaardsbruk.svar.dyr"))
        søknadprosess.flervalg(EgenNæring.`eget gaardsbruk hvem eier`).besvar(Flervalg("faktum.eget-gaardsbruk-hvem-eier.svar.selv", "faktum.eget-gaardsbruk-hvem-eier.svar.ektefelle-samboer"))
        søknadprosess.desimaltall(EgenNæring.`eget gaardsbruk jeg andel inntekt`).besvar(50.0)
        søknadprosess.desimaltall(EgenNæring.`eget gaardsbruk ektefelle samboer andel inntekt`).besvar(50.0)
        søknadprosess.heltall(EgenNæring.`eget gaardsbruk arbeidsaar for timer`).besvar(2020)
        søknadprosess.desimaltall(EgenNæring.`eget gaardsbruk arbeidstimer aar`).besvar(40.5)
        søknadprosess.tekst(EgenNæring.`eget gaardsbruk arbeidstimer beregning`).besvar(Tekst("Brukte kalkulator"))
        assertEquals(true, søknadprosess.resultat())
    }

    @Test
    fun `Verken egen næring eller eget gårdsbruk `() {
        søknadprosess.boolsk(EgenNæring.`driver du egen naering`).besvar(false)
        assertEquals(null, søknadprosess.resultat())

        søknadprosess.boolsk(EgenNæring.`driver du eget gaardsbruk`).besvar(false)
        assertEquals(true, søknadprosess.resultat())
    }
}
