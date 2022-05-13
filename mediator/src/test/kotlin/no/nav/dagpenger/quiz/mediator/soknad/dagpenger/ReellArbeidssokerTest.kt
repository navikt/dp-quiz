package no.nav.dagpenger.quiz.mediator.soknad.dagpenger

import no.nav.dagpenger.model.faktum.Flervalg
import no.nav.dagpenger.model.faktum.Prosessversjon
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.quiz.mediator.helpers.testSøknadprosess
import no.nav.dagpenger.quiz.mediator.soknad.Prosess
import no.nav.dagpenger.quiz.mediator.soknad.verifiserFeltsammensetting
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class ReellArbeidssokerTest {

    @Test
    fun `Sjekk om faktasammensettingen har endret seg siden sist`() {
        ReellArbeidssoker.verifiserFeltsammensetting(9, 45)
    }

    @Test
    fun `regeltre for reel arbeidssøker`() {
        val søknad = Søknad(Prosessversjon(Prosess.Dagpenger, -1), *ReellArbeidssoker.fakta())
        val søknadprosess = søknad.testSøknadprosess(
            ReellArbeidssoker.regeltre(søknad)
        )

        assertEquals(null, søknadprosess.resultat())

        søknadprosess.boolsk(ReellArbeidssoker.`Kan jobbe hel og deltid`).besvar(false)
        assertEquals(null, søknadprosess.resultat())
        søknadprosess.boolsk(ReellArbeidssoker.`Kan jobbe hel og deltid`).besvar(true)
        assertEquals(true, søknadprosess.resultat())
        søknadprosess.flervalg(ReellArbeidssoker.`Årsak til kun deltid`).besvar(Flervalg("faktum.kun-deltid-aarsak.svar.annen-situasjon"))
        assertEquals(true, søknadprosess.resultat())
    }
}
