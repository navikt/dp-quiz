import io.mockk.mockk
import io.mockk.verify
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.UUID

class MediatorTest {

    @Test
    internal fun `leser søknader`() {
        testRapid.sendTestMessage(meldingsfabrikk.ønskerRettighetsavklaring())
        verify {
            hendelseMediator.behandle(
                ofType(ØnskerRettighetsavklaringMelding::class),
                ofType(ØnskerRettighetsavklaring::class)
            )
        }
    }

    @BeforeEach
    internal fun reset() {
        testRapid.reset()
    }

    private companion object {
        private val meldingsfabrikk = TestMeldingFactory("fødselsnummer", "aktør")
        private val testRapid = TestRapid()
        private val hendelseMediator = mockk<HendelseMediator>(relaxed = true)

        init {
            MeldingMediator(
                rapidsConnection = testRapid,
                hendelseRecorder = mockk(relaxed = true),
                hendelseMediator = hendelseMediator
            )
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
