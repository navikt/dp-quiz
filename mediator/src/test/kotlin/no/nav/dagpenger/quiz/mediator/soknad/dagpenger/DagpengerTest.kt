package no.nav.dagpenger.quiz.mediator.soknad.dagpenger

import io.mockk.every
import io.mockk.mockk
import no.nav.dagpenger.model.faktum.Envalg
import no.nav.dagpenger.model.faktum.Land
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.seksjon.Søknadprosess
import no.nav.dagpenger.model.seksjon.Versjon
import no.nav.dagpenger.quiz.mediator.db.ResultatPersistence
import no.nav.dagpenger.quiz.mediator.db.SøknadPersistence
import no.nav.dagpenger.quiz.mediator.integration.SøknadBesvarer
import no.nav.dagpenger.quiz.mediator.meldinger.FaktumSvarService
import no.nav.dagpenger.quiz.mediator.soknad.DslFaktaseksjon
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class DagpengerTest : SøknadBesvarer() {

    private val resultatPersistence = mockk<ResultatPersistence>(relaxed = true)

    private lateinit var søknadsprosess: Søknadprosess

    @BeforeEach
    fun setup() {
        Dagpenger.registrer { prototypeSøknad ->
            søknadsprosess = Versjon.id(Dagpenger.VERSJON_ID)
                .søknadprosess(prototypeSøknad, Versjon.UserInterfaceType.Web)
        }

        val søknadPersistence = mockk<SøknadPersistence>().also {
            every { it.hent(any(), any()) } returns søknadsprosess
            every { it.lagre(any() as Søknad) } returns true
        }

        testRapid = TestRapid().also {
            FaktumSvarService(
                søknadPersistence = søknadPersistence,
                resultatPersistence = resultatPersistence,
                rapidsConnection = it
            )
        }
    }

    @Test
    fun `Skal få seksjonene i riktig rekkefølge, mao slik som designerne har kommet fram til`() {
        withSøknadsId { besvar ->
            søknadsprosess.verifiserAtNesteSeksjonEr(Bosted)
            besvar(Bosted.`hvilket land bor du i`, Land("NOR"))

            søknadsprosess.verifiserAtNesteSeksjonEr(Gjenopptak)
            val harMottattDagpenger = Envalg("faktum.mottatt-dagpenger-siste-12-mnd.svar.ja")
            besvar(Gjenopptak.`mottatt dagpenger siste 12 mnd`, harMottattDagpenger)

            // søknadsprosess.verifiserAtNesteSeksjonEr(Barnetillegg)
            // besvar(Barnetillegg.`barn liste`, 0)

            //TODO: Se jira issue DAG-284
            //søknadsprosess.verifiserAtNesteSeksjonEr(Arbeidsforhold)

            assertEquals(false, søknadsprosess.erFerdig())
        }
    }

    private fun Søknadprosess.verifiserAtNesteSeksjonEr(faktaseksjon: DslFaktaseksjon) {
        assertEquals(faktaseksjon.seksjon(søknad)[0].navn, nesteSeksjoner()[0].navn)
    }
}
