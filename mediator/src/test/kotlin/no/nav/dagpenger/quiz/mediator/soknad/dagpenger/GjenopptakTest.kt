package no.nav.dagpenger.quiz.mediator.soknad.dagpenger

import no.nav.dagpenger.model.faktum.Envalg
import no.nav.dagpenger.model.faktum.Prosessversjon
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.quiz.mediator.helpers.testSøknadprosess
import no.nav.dagpenger.quiz.mediator.soknad.Prosess
import no.nav.dagpenger.quiz.mediator.soknad.verifiserFeltsammensetting
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class GjenopptakTest {

    @Test
    fun `Sjekk om faktasammensettingen har endret seg siden sist`() {
        Gjenopptak.verifiserFeltsammensetting(1, 10001)
    }

    @Test
    fun `Det må svares på om man har mottat dagpenger det siste året`() {
        val søknad = Søknad(Prosessversjon(Prosess.Dagpenger, -1), *Gjenopptak.fakta())
        val søknadprosess = søknad.testSøknadprosess(
            Gjenopptak.regeltre(søknad)
        ) {
            Gjenopptak.seksjon(this)
        }

        assertEquals(null, søknadprosess.resultat())
        søknadprosess.envalg(Gjenopptak.`mottatt dagpenger siste 12 mnd`).besvar(Envalg("faktum.mottatt-dagpenger-siste-12-mnd.svar.ja"))
        assertEquals(true, søknadprosess.resultat())

        søknadprosess.envalg(Gjenopptak.`mottatt dagpenger siste 12 mnd`).besvar(Envalg("faktum.mottatt-dagpenger-siste-12-mnd.svar.nei"))
        assertEquals(true, søknadprosess.resultat())

        søknadprosess.envalg(Gjenopptak.`mottatt dagpenger siste 12 mnd`).besvar(Envalg("faktum.mottatt-dagpenger-siste-12-mnd.svar.vet-ikke"))
        assertEquals(true, søknadprosess.resultat())
    }
}
