
import com.fasterxml.jackson.databind.JsonNode
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.dato
import no.nav.dagpenger.model.fakta.Fakta
import no.nav.dagpenger.model.fakta.Rolle
import no.nav.dagpenger.model.fakta.Rolle.nav
import no.nav.dagpenger.model.regel.er
import no.nav.dagpenger.model.regel.før
import no.nav.dagpenger.model.subsumsjon.alle
import no.nav.dagpenger.model.søknad.Seksjon
import no.nav.dagpenger.model.søknad.Søknad
import no.nav.dagpenger.model.søknad.Versjon
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID
import kotlin.test.assertEquals

internal class BehovMediatorTest {
    private val testRapid = TestRapid()
    private val  mediator = BehovMediator(
        rapidsConnection = testRapid,
    )

    @BeforeEach
    internal fun reset() {
        testRapid.reset()
    }

    @Test
    internal fun `tar imot seksjon og sender ut på kafka`() {
        val fnr = "12345678910"
        val søknad = TestPrototype().søknad(fnr)
        val seksjon = søknad.nesteSeksjon()
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
}

private class TestPrototype {

    companion object {
        const val VERSJON_ID = 1
    }

    private val fakta: Fakta
        get() = Fakta(
            dato faktum "Fødselsdato" id 1,
        )

    val fødselsdato = fakta dato 1

    val inngangsvilkår =
        "Inngangsvilkår".alle(
            "alder".alle(
                fødselsdato er LocalDate.now()
            )
        )

    private val personalia = Seksjon("personalia", nav, fødselsdato)

    val søknad: Søknad =
        Søknad(
            personalia,
        )

    private val versjon = Versjon(VERSJON_ID, fakta, inngangsvilkår, mapOf(Versjon.Type.Web to søknad))

    fun søknad(fnr: String) = versjon.søknad(fnr, Versjon.Type.Web)
}
