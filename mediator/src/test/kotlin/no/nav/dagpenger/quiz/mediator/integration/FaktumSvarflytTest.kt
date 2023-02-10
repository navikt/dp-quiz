package no.nav.dagpenger.quiz.mediator.integration

import io.mockk.every
import io.mockk.mockk
import no.nav.dagpenger.model.faktum.Faktaversjon
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.faktum.Tekst
import no.nav.dagpenger.model.seksjon.Versjon
import no.nav.dagpenger.quiz.mediator.db.ResultatPersistence
import no.nav.dagpenger.quiz.mediator.db.UtredningsprosessRepository
import no.nav.dagpenger.quiz.mediator.helpers.MinimalSøknadsprosess
import no.nav.dagpenger.quiz.mediator.meldinger.FaktumSvarService
import no.nav.dagpenger.quiz.mediator.soknad.Prosessfakta
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.UUID
import kotlin.test.assertEquals

internal class DagpengerFaktumSvarflytTest : SøknadBesvarer() {

    val faktaversjon = Faktaversjon(Prosessfakta.Dagpenger, -2313)
    private val dagpengerSøknadsprosess = MinimalSøknadsprosess(faktaversjon, Rolle.søker)

    private val faktaRepository = mockk<UtredningsprosessRepository>().also {
        every { it.hent(any()) } returns Versjon.id(faktaversjon)
            .utredningsprosess(dagpengerSøknadsprosess.fakta)
        every { it.lagre(any()) } returns true
    }

    private val resultatPersistence = mockk<ResultatPersistence>(relaxed = true)

    @BeforeEach
    fun setup() {
        testRapid = TestRapid().also {
            FaktumSvarService(
                utredningsprosessRepository = faktaRepository,
                resultatPersistence = resultatPersistence,
                rapidsConnection = it,
            )
        }
    }

    @Test
    fun `Sjekker minimalt regeltre for dagpenger`() {
        besvar(UUID.randomUUID().toString(), MinimalSøknadsprosess.faktumBoolsk, true)
        melding(0).also {
            assertEquals("søker_oppgave", it["@event_name"].asText())
        }

        besvar(UUID.randomUUID().toString(), MinimalSøknadsprosess.faktumHeltall, 123)
        melding(1).also {
            assertEquals("søker_oppgave", it["@event_name"].asText())
        }

        besvarTekst(UUID.randomUUID().toString(), MinimalSøknadsprosess.faktumTekst, Tekst("dette er en tekst"))
        melding(2).also {
            assertEquals("søker_oppgave", it["@event_name"].asText())
        }
    }
}

internal class AvslagPåMinsteinntektFaktumSvarflytTest : SøknadBesvarer() {

    val faktaversjon = Faktaversjon(Prosessfakta.AvslagPåMinsteinntekt, -2313)
    private val dagpengerSøknadsprosess = MinimalSøknadsprosess(faktaversjon, Rolle.nav)

    private val faktaRepository = mockk<UtredningsprosessRepository>().also {
        every { it.hent(any()) } returns Versjon.id(faktaversjon)
            .utredningsprosess(dagpengerSøknadsprosess.fakta)
        every { it.lagre(any()) } returns true
    }

    private val resultatPersistence = mockk<ResultatPersistence>(relaxed = true)

    @BeforeEach
    fun setup() {
        testRapid = TestRapid().also {
            FaktumSvarService(
                utredningsprosessRepository = faktaRepository,
                resultatPersistence = resultatPersistence,
                rapidsConnection = it,
            )
        }
    }

    @Test
    fun `Sjekker minimalt regeltre for avslag på minsteinnekt`() {
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
