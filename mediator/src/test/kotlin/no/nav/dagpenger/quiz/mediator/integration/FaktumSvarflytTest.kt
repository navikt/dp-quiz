package no.nav.dagpenger.quiz.mediator.integration

import io.mockk.every
import io.mockk.mockk
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.faktum.Tekst
import no.nav.dagpenger.quiz.mediator.db.ProsessRepository
import no.nav.dagpenger.quiz.mediator.db.ResultatPersistence
import no.nav.dagpenger.quiz.mediator.helpers.MinimalSøknadsprosess
import no.nav.dagpenger.quiz.mediator.helpers.testPerson
import no.nav.dagpenger.quiz.mediator.meldinger.FaktumSvarService
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.UUID
import kotlin.test.assertEquals

internal class FaktumSvarflytTest : SøknadBesvarer() {
    private val henvendelse = MinimalSøknadsprosess(Rolle.nav).bygger()
    private val prosessRepository = mockk<ProsessRepository>().also {
        every { it.hent(any()) } returns henvendelse.prosess(testPerson)
        every { it.lagre(any()) } returns true
    }
    private val resultatPersistence = mockk<ResultatPersistence>(relaxed = true)

    @BeforeEach
    fun setup() {
        testRapid = TestRapid().also {
            FaktumSvarService(
                prosessRepository = prosessRepository,
                resultatPersistence = resultatPersistence,
                rapidsConnection = it,
            )
        }
    }

    @Test
    fun `Sjekker minimalt regeltre for avslag`() {
        besvar(UUID.randomUUID().toString(), MinimalSøknadsprosess.faktumBoolsk, true)

        melding(0).also {
            assertEquals("faktum_svar", it["@event_name"].asText())
        }

        besvar(UUID.randomUUID().toString(), MinimalSøknadsprosess.faktumHeltall, 123)
        melding(1).also {
            assertEquals("faktum_svar", it["@event_name"].asText())
        }

        besvarTekst(UUID.randomUUID().toString(), MinimalSøknadsprosess.faktumTekst, Tekst("dette er en tekst"))

        melding(2).also {
            assertEquals("prosess_resultat", it["@event_name"].asText())
        }
    }
}
