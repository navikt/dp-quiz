package no.nav.dagpenger.quiz.mediator.soknad.dagpenger

import no.nav.dagpenger.model.faktum.Fakta
import no.nav.dagpenger.model.faktum.Faktaversjon
import no.nav.dagpenger.model.faktum.Land
import no.nav.dagpenger.model.faktum.Tekst
import no.nav.dagpenger.model.seksjon.Utredningsprosess
import no.nav.dagpenger.quiz.mediator.helpers.testSøknadprosess
import no.nav.dagpenger.quiz.mediator.soknad.Prosessfakta
import no.nav.dagpenger.quiz.mediator.soknad.dagpenger.Barnetillegg.`barn etternavn`
import no.nav.dagpenger.quiz.mediator.soknad.dagpenger.Barnetillegg.`barn etternavn register`
import no.nav.dagpenger.quiz.mediator.soknad.dagpenger.Barnetillegg.`barn fornavn mellomnavn`
import no.nav.dagpenger.quiz.mediator.soknad.dagpenger.Barnetillegg.`barn fornavn mellomnavn register`
import no.nav.dagpenger.quiz.mediator.soknad.dagpenger.Barnetillegg.`barn fødselsdato`
import no.nav.dagpenger.quiz.mediator.soknad.dagpenger.Barnetillegg.`barn fødselsdato register`
import no.nav.dagpenger.quiz.mediator.soknad.dagpenger.Barnetillegg.`barn liste`
import no.nav.dagpenger.quiz.mediator.soknad.dagpenger.Barnetillegg.`barn liste register`
import no.nav.dagpenger.quiz.mediator.soknad.dagpenger.Barnetillegg.`barn statsborgerskap`
import no.nav.dagpenger.quiz.mediator.soknad.dagpenger.Barnetillegg.`barn statsborgerskap register`
import no.nav.dagpenger.quiz.mediator.soknad.dagpenger.Barnetillegg.`egne barn`
import no.nav.dagpenger.quiz.mediator.soknad.dagpenger.Barnetillegg.`forsørger du barnet`
import no.nav.dagpenger.quiz.mediator.soknad.dagpenger.Barnetillegg.`forsørger du barnet register`
import no.nav.dagpenger.quiz.mediator.soknad.verifiserFeltsammensetting
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDate
import kotlin.test.assertTrue

internal class BarnetilleggTest {
    private val fakta = Fakta(Faktaversjon(Prosessfakta.Dagpenger, -1), *Barnetillegg.fakta())
    private lateinit var utredningsprosess: Utredningsprosess

    @BeforeEach
    fun setup() {
        utredningsprosess = fakta.testSøknadprosess(
            Barnetillegg.regeltre(fakta),
        ) {
            Barnetillegg.seksjon(this)
        }
    }

    @Test
    fun `Sjekk om faktasammensettingen har endret seg siden sist`() {
        Barnetillegg.verifiserFeltsammensetting(15, 15120)
    }

    @Test
    fun `Må ikke besvare noe om vi ikke finner noen barn i registeret`() {
        utredningsprosess.generator(`barn liste register`).besvar(0)
        utredningsprosess.boolsk(`egne barn`).besvar(false)
        assertEquals(true, utredningsprosess.resultat())
    }

    @Test
    fun `Søker har ingen barn i registeret men registrerer 1 barn manuelt`() {
        utredningsprosess.generator(`barn liste register`).besvar(0)
        utredningsprosess.boolsk(`egne barn`).besvar(true)
        utredningsprosess.generator(`barn liste`).besvar(1)
        assertEquals(null, utredningsprosess.resultat())

        utredningsprosess.tekst("${`barn fornavn mellomnavn`}.1").besvar(Tekst("Ola"))
        utredningsprosess.tekst("${`barn etternavn`}.1").besvar(Tekst("Nordmann"))
        utredningsprosess.dato("${`barn fødselsdato`}.1").besvar(LocalDate.now().minusYears(1))
        utredningsprosess.land("${`barn statsborgerskap`}.1").besvar(Land("NOR"))
        utredningsprosess.boolsk("${`forsørger du barnet`}.1").besvar(false)
        assertEquals(true, utredningsprosess.resultat())

        utredningsprosess.boolsk("${`forsørger du barnet`}.1").besvar(true)

        assertEquals(true, utredningsprosess.resultat())
    }

    @Test
    fun `Søker har 1 barn i registeret`() {
        utredningsprosess.generator(`barn liste register`).besvar(1)
        utredningsprosess.boolsk(`egne barn`).besvar(false)
        utredningsprosess.generator(`barn liste`).besvar(0)
        assertEquals(null, utredningsprosess.resultat())

        utredningsprosess.tekst("${`barn fornavn mellomnavn register`}.1").besvar(Tekst("Ola"))
        utredningsprosess.tekst("${`barn etternavn register`}.1").besvar(Tekst("Nordmann"))
        utredningsprosess.dato("${`barn fødselsdato register`}.1").besvar(LocalDate.now().minusYears(1))
        utredningsprosess.land("${`barn statsborgerskap register`}.1").besvar(Land("NOR"))
        val seksjon = utredningsprosess.nesteSeksjoner().first()
        assertEquals("barnetillegg", seksjon.navn)
        assertTrue(seksjon.contains(utredningsprosess.boolsk("${`forsørger du barnet register`}.1")))

        utredningsprosess.boolsk("${`forsørger du barnet register`}.1").besvar(false)
        assertEquals(true, utredningsprosess.resultat())

        utredningsprosess.boolsk("${`forsørger du barnet register`}.1").besvar(true)

        assertEquals(true, utredningsprosess.resultat())
    }

    @Test
    fun `Faktarekkefølge i seksjon`() {
        val faktaFraRegister =
            utredningsprosess.nesteSeksjoner().first().joinToString(separator = ",") { it.id }
        assertEquals("1008,1009,1010,1012,1011", faktaFraRegister)

        utredningsprosess.generator(`barn liste register`).besvar(0)
        val faktaForSøker =
            utredningsprosess.nesteSeksjoner().first().joinToString(separator = ",") { it.id }
        assertEquals("1013,1007,1001,1002,1003,1004,1005,1006,1014", faktaForSøker)
    }
}
