package no.nav.dagpenger.quiz.mediator.meldinger
import no.nav.dagpenger.model.seksjon.Versjon.UserInterfaceType.Web
import no.nav.dagpenger.quiz.mediator.helpers.SøknadEksempel
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class BehovMediatorTest {
    private val fnr = "12345678910"
    private val testRapid = TestRapid()
    private val mediator = BehovMediator(
        rapidsConnection = testRapid,
    )

    @BeforeEach
    internal fun reset() {
        testRapid.reset()
    }

    @Test
    fun `tar imot seksjon og sender ut på kafka`() {
        val søknadprosess = SøknadEksempel.v.søknadprosess(fnr, Web)
        val seksjon = søknadprosess.nesteSeksjoner().first()
        mediator.håndter(seksjon, fnr, søknadprosess.søknad.uuid)
        assertEquals(1, testRapid.inspektør.size)

        testRapid.inspektør.message(0).also {
            assertTrue(it.has("fnr"))
            assertTrue(it.has("fakta"))
            assertTrue(it.has("søknadUuid"))
            assertTrue(it.has("seksjonsnavn"))

            assertEquals(fnr, it["fnr"].asText())
        }
    }
}
