package no.nav.dagpenger.quiz.mediator.soknad.dagpenger

import no.nav.dagpenger.model.faktum.Land
import no.nav.dagpenger.model.faktum.Prosessversjon
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.faktum.Tekst
import no.nav.dagpenger.model.seksjon.Søknadprosess
import no.nav.dagpenger.quiz.mediator.helpers.testSøknadprosess
import no.nav.dagpenger.quiz.mediator.soknad.Prosess
import no.nav.dagpenger.quiz.mediator.soknad.dagpenger.Barnetillegg.`barn etternavn`
import no.nav.dagpenger.quiz.mediator.soknad.dagpenger.Barnetillegg.`barn etternavn register`
import no.nav.dagpenger.quiz.mediator.soknad.dagpenger.Barnetillegg.`barn foedselsdato`
import no.nav.dagpenger.quiz.mediator.soknad.dagpenger.Barnetillegg.`barn foedselsdato register`
import no.nav.dagpenger.quiz.mediator.soknad.dagpenger.Barnetillegg.`barn fornavn mellomnavn`
import no.nav.dagpenger.quiz.mediator.soknad.dagpenger.Barnetillegg.`barn fornavn mellomnavn register`
import no.nav.dagpenger.quiz.mediator.soknad.dagpenger.Barnetillegg.`barn liste`
import no.nav.dagpenger.quiz.mediator.soknad.dagpenger.Barnetillegg.`barn liste register`
import no.nav.dagpenger.quiz.mediator.soknad.dagpenger.Barnetillegg.`barn statsborgerskap`
import no.nav.dagpenger.quiz.mediator.soknad.dagpenger.Barnetillegg.`barn statsborgerskap register`
import no.nav.dagpenger.quiz.mediator.soknad.dagpenger.Barnetillegg.`egne barn`
import no.nav.dagpenger.quiz.mediator.soknad.dagpenger.Barnetillegg.`forsoerger du barnet`
import no.nav.dagpenger.quiz.mediator.soknad.dagpenger.Barnetillegg.`forsoerger du barnet register`
import no.nav.dagpenger.quiz.mediator.soknad.verifiserFeltsammensetting
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDate
import kotlin.test.assertTrue

internal class BarnetilleggTest {
    private val søknad = Søknad(Prosessversjon(Prosess.Dagpenger, -1), *Barnetillegg.fakta())
    private lateinit var søknadprosess: Søknadprosess

    @BeforeEach
    fun setup() {
        søknadprosess = søknad.testSøknadprosess(
            Barnetillegg.regeltre(søknad)
        ) {
            Barnetillegg.seksjon(this)
        }
    }

    @Test
    fun `Sjekk om faktasammensettingen har endret seg siden sist`() {
        Barnetillegg.verifiserFeltsammensetting(13, 13091)
    }

    @Test
    fun `Må ikke besvare noe om vi ikke finner noen barn i registeret`() {
        søknadprosess.generator(`barn liste register`).besvar(0)
        søknadprosess.boolsk(`egne barn`).besvar(false)
        assertEquals(true, søknadprosess.resultat())
    }

    @Test
    fun `Søker har ingen barn i registeret men registrerer 1 barn manuelt`() {
        søknadprosess.generator(`barn liste register`).besvar(0)
        søknadprosess.boolsk(`egne barn`).besvar(true)
        søknadprosess.generator(`barn liste`).besvar(1)
        assertEquals(null, søknadprosess.resultat())

        søknadprosess.tekst("${`barn fornavn mellomnavn`}.1").besvar(Tekst("Ola"))
        søknadprosess.tekst("${`barn etternavn`}.1").besvar(Tekst("Nordmann"))
        søknadprosess.dato("${`barn foedselsdato`}.1").besvar(LocalDate.now().minusYears(1))
        søknadprosess.land("${`barn statsborgerskap`}.1").besvar(Land("NOR"))
        søknadprosess.boolsk("${`forsoerger du barnet`}.1").besvar(false)
        assertEquals(true, søknadprosess.resultat())

        søknadprosess.boolsk("${`forsoerger du barnet`}.1").besvar(true)

        assertEquals(true, søknadprosess.resultat())
    }

    @Test
    fun `Søker har 1 barn i registeret`() {
        søknadprosess.generator(`barn liste register`).besvar(1)
        søknadprosess.boolsk(`egne barn`).besvar(false)
        søknadprosess.generator(`barn liste`).besvar(0)
        assertEquals(null, søknadprosess.resultat())

        søknadprosess.tekst("${`barn fornavn mellomnavn register`}.1").besvar(Tekst("Ola"))
        søknadprosess.tekst("${`barn etternavn register`}.1").besvar(Tekst("Nordmann"))
        søknadprosess.dato("${`barn foedselsdato register`}.1").besvar(LocalDate.now().minusYears(1))
        søknadprosess.land("${`barn statsborgerskap register`}.1").besvar(Land("NOR"))
        val seksjon = søknadprosess.nesteSeksjoner().first()
        assertEquals("barnetillegg", seksjon.navn)
        assertTrue(seksjon.contains(søknadprosess.boolsk("${`forsoerger du barnet register`}.1")))

        søknadprosess.boolsk("${`forsoerger du barnet register`}.1").besvar(false)
        assertEquals(true, søknadprosess.resultat())

        søknadprosess.boolsk("${`forsoerger du barnet register`}.1").besvar(true)

        assertEquals(true, søknadprosess.resultat())
    }
}
