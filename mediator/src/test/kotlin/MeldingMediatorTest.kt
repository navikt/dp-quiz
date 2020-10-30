import db.SøknadPersistance
import io.mockk.mockk
import no.nav.dagpenger.model.faktagrupper.Faktagrupper
import no.nav.dagpenger.model.faktagrupper.Versjon
import no.nav.dagpenger.model.faktum.Faktum
import no.nav.dagpenger.model.faktum.Søknad
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
    @Disabled
    internal fun `Oppretter faktagrupper og persisterer noe ved ønsket rettighetsavklaring`() {
        testRapid.sendTestMessage(meldingsfabrikk.ønskerRettighetsavklaring())
        assertEquals(1, grupperer.faktaList.size)
    }

    @Test
    @Disabled
    fun `Publiserer noe ved ønsket rettighetsavklaring`() {
        testRapid.sendTestMessage(meldingsfabrikk.ønskerRettighetsavklaring())
        assertEquals(1, testRapid.inspektør.size)
    }

    @BeforeEach
    internal fun reset() {
        testRapid.reset()
        grupperer.faktaList.clear()
    }

    private companion object {
        private val meldingsfabrikk = TestMeldingFactory("fødselsnummer", "aktør")
        private val testRapid = TestRapid()
        private val grupperer = TestFaktagrupperer()
        private val hendelseMediator = HendelseMediator(grupperer, testRapid)

        init {
            MeldingMediator(
                rapidsConnection = testRapid,
                hendelseRecorder = mockk(relaxed = true),
                hendelseMediator = hendelseMediator
            )
        }
    }

    private class TestFaktagrupperer : SøknadPersistance {
        val faktaList = mutableMapOf<UUID, List<Faktum<*>>>()
        override fun ny(fnr: String, type: Versjon.FaktagrupperType): Faktagrupper {
            TODO("Not yet implemented")
        }

        override fun hent(uuid: UUID, type: Versjon.FaktagrupperType): Faktagrupper {
            TODO("Not yet implemented")
        }

        override fun lagre(søknad: Søknad): Boolean {
            TODO("Not yet implemented")
        }

        override fun opprettede(fnr: String): Map<LocalDateTime, UUID> {
            TODO("Not yet implemented")
        }
    }
}

private class TestMeldingFactory(private val fødselsnummer: String, private val aktørId: String) {
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
