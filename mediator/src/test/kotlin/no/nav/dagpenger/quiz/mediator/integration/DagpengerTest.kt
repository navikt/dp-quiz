package no.nav.dagpenger.quiz.mediator.integration

import io.mockk.every
import io.mockk.mockk
import no.nav.dagpenger.model.faktum.Envalg
import no.nav.dagpenger.model.faktum.Fakta
import no.nav.dagpenger.model.faktum.Land
import no.nav.dagpenger.model.seksjon.Utredningsprosess
import no.nav.dagpenger.model.seksjon.Versjon
import no.nav.dagpenger.quiz.mediator.db.FaktaRepository
import no.nav.dagpenger.quiz.mediator.db.ResultatPersistence
import no.nav.dagpenger.quiz.mediator.meldinger.FaktumSvarService
import no.nav.dagpenger.quiz.mediator.soknad.DslFaktaseksjon
import no.nav.dagpenger.quiz.mediator.soknad.dagpenger.Bosted
import no.nav.dagpenger.quiz.mediator.soknad.dagpenger.Dagpenger
import no.nav.dagpenger.quiz.mediator.soknad.dagpenger.DinSituasjon
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class DagpengerTest : SøknadBesvarer() {
    private val resultatPersistence = mockk<ResultatPersistence>(relaxed = true)
    private lateinit var søknadsprosess: Utredningsprosess

    @BeforeEach
    fun setup() {
        Dagpenger.registrer { prototypeSøknad ->
            søknadsprosess = Versjon.id(Dagpenger.VERSJON_ID)
                .utredningsprosess(prototypeSøknad)
        }
        val faktaRepository = mockk<FaktaRepository>().also {
            every { it.hent(any()) } returns søknadsprosess
            every { it.lagre(any() as Fakta) } returns true
        }

        testRapid = TestRapid().also {
            FaktumSvarService(
                faktaRepository = faktaRepository,
                resultatPersistence = resultatPersistence,
                rapidsConnection = it,
            )
        }
    }

    @Test
    fun `Skal få seksjonene i riktig rekkefølge, mao slik som designerne har kommet fram til`() {
        withSøknadsId { besvar ->
            søknadsprosess.verifiserAtNesteSeksjonEr(Bosted)
            besvar(Bosted.`hvilket land bor du i`, Land("NOR"))

            søknadsprosess.verifiserAtNesteSeksjonEr(DinSituasjon)
            val harMottattDagpenger = Envalg("faktum.mottatt-dagpenger-siste-12-mnd.svar.ja")
            besvar(DinSituasjon.`mottatt dagpenger siste 12 mnd`, harMottattDagpenger)
            // søknadsprosess.verifiserAtNesteSeksjonEr(Arbeidsforhold)
            assertEquals(false, søknadsprosess.erFerdig())
        }
    }

    private fun Utredningsprosess.verifiserAtNesteSeksjonEr(faktaseksjon: DslFaktaseksjon) {
        assertNotEquals(nesteSeksjoner().size, 0, "Har ikke neste seksjon")
        assertEquals(faktaseksjon.seksjon(fakta)[0].navn, nesteSeksjoner()[0].navn)
    }
}
