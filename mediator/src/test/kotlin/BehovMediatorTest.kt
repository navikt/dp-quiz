
import com.fasterxml.jackson.databind.JsonNode
import no.nav.dagpenger.model.marshalling.SeksjonJsonBuilder
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import utils.AvhengerAvTestPrototype
import utils.FødselsdatoTestPrototype
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
        val faktagrupper = FødselsdatoTestPrototype().faktagrupper(fnr)
        val seksjon = faktagrupper.nesteSeksjon()
        mediator.håndter(seksjon, fnr)
        assertEquals(1, testRapid.inspektør.size)

        testRapid.inspektør.message(0).also {
            assertTrue(it.has("@behov"))
            assertTrue(it["@behov"].isArray)
            assertTrue(it["@behov"].map(JsonNode::asText).contains("Fødselsdato"))
            assertTrue(it.has("fødselsnummer"))
            assertEquals(fnr, it["fødselsnummer"].asText())
        }
    }

    @Test
    fun `sender behov med avhengige fakta`() {
        val faktagrupper = AvhengerAvTestPrototype().delvisBesvartSøknad(fnr)
        val seksjon = faktagrupper.nesteSeksjon()
        mediator.håndter(seksjon, fnr)

        SeksjonJsonBuilder(seksjon).resultat()["fakta"]

        testRapid.inspektør.message(0).also {
            assertTrue(it["@behov"].map(JsonNode::asText).containsAll(listOf("InntektSisteÅr", "InntektSiste3År")))
            assertTrue(it.has("fakta"))
            println(it)
            assertTrue(it["fakta"].any { it["navn"].asText() == "Virkningstidspunkt" })
        }
    }
}
