
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
    @BeforeEach
    internal fun reset() {
        testRapid.reset()
    }

    private companion object {
        private val testRapid = TestRapid()
        private lateinit var mediator: BehovMediator

        init {
            mediator = BehovMediator(
                rapidsConnection = testRapid,
            )
        }
    }

    @Test
    internal fun `tar imot seksjon og sender ut på kafka`() {
        val fnr = "12345678910"
        val søknad = TestPrototype().søknad(fnr)
        val seksjon = søknad.nesteSeksjon()
        mediator.håndter(seksjon, fnr)
        assertEquals(1, testRapid.inspektør.size)

        val behovmelding = TestBehovMeldingFactory(fnr).behovsMelding(listOf("fødselsdato"))
        assertTrue(testRapid.inspektør.field(0, "@behov").map(JsonNode::asText).contains("Fødselsdato"))
        assertEquals(fnr, testRapid.inspektør.field(0, "fødselsnummer").asText())
    }
}

private class TestBehovMeldingFactory(private val fødselsnummer: String) {
    fun behovsMelding(behov: List<String>): String = nyHendelse(
        "behov",
        mapOf(
            "@behov" to behov.toString(),
            "fødselsnummer" to fødselsnummer
        )
    )

    private fun nyHendelse(navn: String, hendelse: Map<String, Any>) =
        JsonMessage.newMessage(nyHendelse(navn) + hendelse).toJson()

    private fun nyHendelse(navn: String) = mutableMapOf<String, Any>(
        "@id" to UUID.randomUUID(),
        "@event_name" to navn,
        "@opprettet" to LocalDateTime.now()
    )
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
