import db.Søknader
import io.mockk.mockk
import no.nav.dagpenger.model.søknad.Søknad
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.UUID

class MeldingMediatorTest {

    @Test
    internal fun `Oppretter søknad`() {
        testRapid.sendTestMessage(meldingsfabrikk.ønskerRettighetsavklaring())
        assertEquals(1, søknader.søknader.size)
    }

    @Test fun `Publiserer behov`() {
        testRapid.sendTestMessage(meldingsfabrikk.ønskerRettighetsavklaring())
        assertEquals(1, testRapid.inspektør.size)
    }

    @BeforeEach
    internal fun reset() {
        testRapid.reset()
    }

    private companion object {
        private val meldingsfabrikk = TestMeldingFactory("fødselsnummer", "aktør")
        private val testRapid = TestRapid()
        private val søknader = TestSøknader()
        private val hendelseMediator = HendelseMediator(søknader, testRapid)

        init {
            MeldingMediator(
                rapidsConnection = testRapid,
                hendelseRecorder = mockk(relaxed = true),
                hendelseMediator = hendelseMediator
            )
        }
    }

    private class TestSøknader : Søknader {
        val søknader = mutableMapOf<UUID, Søknad>()
        override fun nySøknad(søknad: Søknad) {
            søknader[UUID.randomUUID()] = søknad
        }
    }
}

class TestMeldingFactory(private val fødselsnummer: String, private val aktørId: String) {
    fun ønskerRettighetsavklaring(): String = nyHendelse(
        "ønsker_rettighetsavklaring",
        mapOf(
            "aktørId" to aktørId,
            "fødselsnummer" to fødselsnummer,
            "avklaringsId" to UUID.randomUUID(),
            "opprettet" to LocalDateTime.now()
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
