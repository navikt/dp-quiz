package no.nav.dagpenger.quiz.mediator.soknad.dagpenger

import no.nav.dagpenger.model.faktum.Fakta
import no.nav.dagpenger.model.faktum.Land
import no.nav.dagpenger.model.faktum.Tekst
import no.nav.dagpenger.model.seksjon.Prosess
import no.nav.dagpenger.quiz.mediator.helpers.testFaktaversjon
import no.nav.dagpenger.quiz.mediator.helpers.testSøknadprosess
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
    private val fakta = Fakta(testFaktaversjon(), *Barnetillegg.fakta())
    private lateinit var prosess: Prosess

    @BeforeEach
    fun setup() {
        prosess = fakta.testSøknadprosess(
            subsumsjon = Barnetillegg.regeltre(fakta),
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
        prosess.generator(`barn liste register`).besvar(0)
        prosess.boolsk(`egne barn`).besvar(false)
        assertEquals(true, prosess.resultat())
    }

    @Test
    fun `Søker har ingen barn i registeret men registrerer 1 barn manuelt`() {
        prosess.generator(`barn liste register`).besvar(0)
        prosess.boolsk(`egne barn`).besvar(true)
        prosess.generator(`barn liste`).besvar(1)
        assertEquals(null, prosess.resultat())

        prosess.tekst("${`barn fornavn mellomnavn`}.1").besvar(Tekst("Ola"))
        prosess.tekst("${`barn etternavn`}.1").besvar(Tekst("Nordmann"))
        prosess.dato("${`barn fødselsdato`}.1").besvar(LocalDate.now().minusYears(1))
        prosess.land("${`barn statsborgerskap`}.1").besvar(Land("NOR"))
        prosess.boolsk("${`forsørger du barnet`}.1").besvar(false)
        assertEquals(true, prosess.resultat())

        prosess.boolsk("${`forsørger du barnet`}.1").besvar(true)

        assertEquals(true, prosess.resultat())
    }

    @Test
    fun `Søker har 1 barn i registeret`() {
        prosess.generator(`barn liste register`).besvar(1)
        prosess.boolsk(`egne barn`).besvar(false)
        prosess.generator(`barn liste`).besvar(0)
        assertEquals(null, prosess.resultat())

        prosess.tekst("${`barn fornavn mellomnavn register`}.1").besvar(Tekst("Ola"))
        prosess.tekst("${`barn etternavn register`}.1").besvar(Tekst("Nordmann"))
        prosess.dato("${`barn fødselsdato register`}.1").besvar(LocalDate.now().minusYears(1))
        prosess.land("${`barn statsborgerskap register`}.1").besvar(Land("NOR"))
        val seksjon = prosess.nesteSeksjoner().first()
        assertEquals("barnetillegg", seksjon.navn)
        assertTrue(seksjon.contains(prosess.boolsk("${`forsørger du barnet register`}.1")))

        prosess.boolsk("${`forsørger du barnet register`}.1").besvar(false)
        assertEquals(true, prosess.resultat())

        prosess.boolsk("${`forsørger du barnet register`}.1").besvar(true)

        assertEquals(true, prosess.resultat())
    }

    @Test
    fun `Faktarekkefølge i seksjon`() {
        val faktaFraRegister =
            prosess.nesteSeksjoner().first().joinToString(separator = ",") { it.id }
        assertEquals("1008,1009,1010,1012,1011", faktaFraRegister)

        prosess.generator(`barn liste register`).besvar(0)
        val faktaForSøker =
            prosess.nesteSeksjoner().first().joinToString(separator = ",") { it.id }
        assertEquals("1013,1007,1001,1002,1003,1004,1005,1006,1014", faktaForSøker)
    }
}
