
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.dato
import no.nav.dagpenger.model.fakta.Fakta
import no.nav.dagpenger.model.fakta.Rolle
import no.nav.dagpenger.model.fakta.Rolle.nav
import no.nav.dagpenger.model.regel.før
import no.nav.dagpenger.model.subsumsjon.alle
import no.nav.dagpenger.model.søknad.Seksjon
import no.nav.dagpenger.model.søknad.Søknad
import no.nav.dagpenger.model.søknad.Versjon
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.UUID
import kotlin.test.assertEquals

internal class BehovMediatorTest {
    @BeforeEach
    internal fun reset() {
        testRapid.reset()
    }

    private companion object {
        private val meldingsfabrikk = TestBehovMeldingFactory("fødselsnummer")
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
        val søknad = TestPrototype().søknad("12345678910")
        val seksjon = søknad.nesteSeksjon()
        mediator.håndter(seksjon)
        assertEquals(1, testRapid.inspektør.size)
        assertEquals(TestBehovMeldingFactory("12345678910").behovsMelding(), testRapid.inspektør.message(0))
    }
}

private class TestBehovMeldingFactory(private val fødselsnummer: String) {
    fun behovsMelding(): String = nyHendelse(
        "behov",
        mapOf(
            "@behov" to listOf("Personalia"),
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
            dato faktum "Ønsker dagpenger fra dato" id 2,
            dato faktum "Dato for bortfall på grunn av alder" id 3,
        )

    val fødselsdato = fakta dato 1

    val virkningstidspunkt = fakta dato 2

    val datoForBortfallPgaAlder = fakta dato 3

    val inngangsvilkår =
        "Inngangsvilkår".alle(
            "alder".alle(
                virkningstidspunkt før datoForBortfallPgaAlder,
            )
        )

    private val personalia = Seksjon("personalia", Rolle.nav, fødselsdato, virkningstidspunkt, datoForBortfallPgaAlder)

    internal val søknad: Søknad =
        Søknad(
            personalia,
        )

    private val versjon = Versjon(VERSJON_ID, fakta, inngangsvilkår, mapOf(Versjon.Type.Web to søknad))

    fun søknad(fnr: String) = versjon.søknad(fnr, Versjon.Type.Web)
}
