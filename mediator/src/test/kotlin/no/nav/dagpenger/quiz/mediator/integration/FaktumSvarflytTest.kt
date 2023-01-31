package no.nav.dagpenger.quiz.mediator.integration

import io.mockk.every
import io.mockk.mockk
import no.nav.dagpenger.model.faktum.Fakta
import no.nav.dagpenger.model.faktum.HenvendelsesType
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.faktum.Tekst
import no.nav.dagpenger.model.seksjon.Versjon
import no.nav.dagpenger.quiz.mediator.db.ResultatPersistence
import no.nav.dagpenger.quiz.mediator.db.SøknadPersistence
import no.nav.dagpenger.quiz.mediator.helpers.MinimalSøknadsprosess
import no.nav.dagpenger.quiz.mediator.meldinger.FaktumSvarService
import no.nav.dagpenger.quiz.mediator.soknad.Prosess
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.UUID
import kotlin.test.assertEquals

internal class DagpengerFaktumSvarflytTest : SøknadBesvarer() {

    val henvendelsesType = HenvendelsesType(Prosess.Dagpenger, -2313)
    private val dagpengerSøknadsprosess = MinimalSøknadsprosess(henvendelsesType, Rolle.søker)

    private val faktaPersistence = mockk<SøknadPersistence>().also {
        every { it.hent(any()) } returns Versjon.id(henvendelsesType)
            .søknadprosess(dagpengerSøknadsprosess.fakta)
        every { it.lagre(any() as Fakta) } returns true
    }

    private val resultatPersistence = mockk<ResultatPersistence>(relaxed = true)

    @BeforeEach
    fun setup() {
        testRapid = TestRapid().also {
            FaktumSvarService(
                søknadPersistence = faktaPersistence,
                resultatPersistence = resultatPersistence,
                rapidsConnection = it
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

    val henvendelsesType = HenvendelsesType(Prosess.AvslagPåMinsteinntekt, -2313)
    private val dagpengerSøknadsprosess = MinimalSøknadsprosess(henvendelsesType, Rolle.nav)

    private val faktaPersistence = mockk<SøknadPersistence>().also {
        every { it.hent(any()) } returns Versjon.id(henvendelsesType)
            .søknadprosess(dagpengerSøknadsprosess.fakta)
        every { it.lagre(any() as Fakta) } returns true
    }

    private val resultatPersistence = mockk<ResultatPersistence>(relaxed = true)

    @BeforeEach
    fun setup() {
        testRapid = TestRapid().also {
            FaktumSvarService(
                søknadPersistence = faktaPersistence,
                resultatPersistence = resultatPersistence,
                rapidsConnection = it
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
