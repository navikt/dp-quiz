package no.nav.dagpenger.quiz.mediator.soknad.dagpenger

import no.nav.dagpenger.model.faktum.Dokument
import no.nav.dagpenger.model.faktum.Prosessversjon
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.faktum.Tekst
import no.nav.dagpenger.model.seksjon.Søknadprosess
import no.nav.dagpenger.quiz.mediator.helpers.testSøknadprosess
import no.nav.dagpenger.quiz.mediator.soknad.Prosess
import no.nav.dagpenger.quiz.mediator.soknad.verifiserFeltsammensetting
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDate
import kotlin.test.assertEquals

internal class DokumentasjonsKravForVernepliktTest {

    private val dokumentfakta = Verneplikt.fakta() + DokumentasjonskravVerneplikt.fakta()
    private val søknad = Søknad(Prosessversjon(Prosess.Dagpenger, -1), *dokumentfakta)
    private lateinit var søknadprosess: Søknadprosess

    @Test
    fun `Sjekk om faktasammensettingen har endret seg siden sist`() {
        DokumentasjonskravVerneplikt.verifiserFeltsammensetting(3, 33006)
    }

    @BeforeEach
    fun setup() {
        søknadprosess = søknad.testSøknadprosess(
            DokumentasjonskravVerneplikt.regeltre(søknad)
        ) {
            DokumentasjonskravVerneplikt.seksjon(this)
        }
    }

    @Test
    fun `Har ikke avtjent verneplikt`() {
        søknadprosess.boolsk(Verneplikt.`avtjent militaer sivilforsvar tjeneste siste 12 mnd`).besvar(false)
        assertEquals(true, søknadprosess.resultat())
    }

    @Test
    fun `Har avtjent verneplikt og kan laster opp tjenestebevis`() {
        søknadprosess.boolsk(Verneplikt.`avtjent militaer sivilforsvar tjeneste siste 12 mnd`).besvar(true)
        søknadprosess.boolsk(DokumentasjonskravVerneplikt.`tjenestebevis tilgjengelig`).besvar(true)
        søknadprosess.dokument(DokumentasjonskravVerneplikt.`tjenestebevis for avtjent verneplikt`).besvar(Dokument(lastOppTidsstempel = LocalDate.now(), "urn:nav:vedlegg:1"))
        assertEquals(true, søknadprosess.resultat())
    }

    @Test
    fun `Har avtjent verneplikt og har ikke tjenestebevis`() {
        søknadprosess.boolsk(Verneplikt.`avtjent militaer sivilforsvar tjeneste siste 12 mnd`).besvar(true)
        søknadprosess.boolsk(DokumentasjonskravVerneplikt.`tjenestebevis tilgjengelig`).besvar(false)
        søknadprosess.tekst(DokumentasjonskravVerneplikt.`tjenestebevis ikke tilgjengelig årsak`).besvar(Tekst("Kan ikke laste opp det nå, sender det senere. kanskje"))
        assertEquals(true, søknadprosess.resultat())
    }
}
