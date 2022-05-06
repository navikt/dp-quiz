package no.nav.dagpenger.quiz.mediator.integration

import io.mockk.every
import io.mockk.mockk
import no.nav.dagpenger.model.faktum.Prosessversjon
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.seksjon.Versjon
import no.nav.dagpenger.quiz.mediator.db.ResultatPersistence
import no.nav.dagpenger.quiz.mediator.db.SøknadPersistence
import no.nav.dagpenger.quiz.mediator.helpers.MinimalSøknadsprosess
import no.nav.dagpenger.quiz.mediator.meldinger.FaktumSvarService
import no.nav.dagpenger.quiz.mediator.meldinger.FaktumSvarServiceTest
import no.nav.dagpenger.quiz.mediator.soknad.Prosess
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.junit.jupiter.api.Test

internal class FaktumFlytSvarTest {

    val prosessversjon = Prosessversjon(Prosess.Dagpenger, -2313)
    private val dagpengerSøknadsprosess = MinimalSøknadsprosess(prosessversjon)

    private val søknadPersistence = mockk<SøknadPersistence>().also {
        every { it.hent(any(), any()) } returns Versjon.id(prosessversjon)
            .søknadprosess(dagpengerSøknadsprosess.søknad, Versjon.UserInterfaceType.Web)
        every { it.lagre(any() as Søknad) } returns true
    }

    val resultatPersistence = mockk<ResultatPersistence>(relaxed = true)

    val testRapid = TestRapid().also {
        FaktumSvarService(
            søknadPersistence = søknadPersistence,
            resultatPersistence = resultatPersistence,
            rapidsConnection = it
        )
    }

    @Test
    fun `Sjekker minimalt regeltre for dagpenger`() {
    }
}