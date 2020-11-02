import db.SøknadPersistance
import helpers.SøknadEksempel
import helpers.desember
import io.mockk.mockk
import no.nav.dagpenger.model.faktagrupper.Faktagrupper
import no.nav.dagpenger.model.faktagrupper.Versjon
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.UUID

class MeldingMediatorTest {

    @BeforeEach
    internal fun reset() {
        testRapid.reset()
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
            SøknadEksempel
        }
    }

    @Test
    internal fun `Oppretter faktagrupper og persisterer noe ved ønsket rettighetsavklaring`() {
        testRapid.sendTestMessage(meldingsfabrikk.ønskerRettighetsavklaring())
        assertEquals(1, testRapid.inspektør.size)
    }

    @Test
    internal fun `ta imot svar`() {
        testRapid.sendTestMessage(meldingsfabrikk.ønskerRettighetsavklaring())
        val uuid = UUID.fromString(testRapid.inspektør.message(0)["søknadId"].asText())
        testRapid.sendTestMessage(meldingsfabrikk.besvarFaktum(uuid, 1, "boolean", "true"))
        testRapid.sendTestMessage(meldingsfabrikk.besvarFaktum(uuid, 3, "heltall", "2"))
        testRapid.sendTestMessage(meldingsfabrikk.besvarFaktum(uuid, 4, "dato", 24.desember.toString()))
        assertEquals(4, testRapid.inspektør.size)
    }

    private class TestFaktagrupperer : SøknadPersistance {
        private lateinit var faktagrupper: Faktagrupper

        override fun ny(fnr: String, type: Versjon.FaktagrupperType): Faktagrupper {
            return Versjon.siste.faktagrupper(fnr, type).also { faktagrupper = it }
        }

        override fun hent(uuid: UUID, type: Versjon.FaktagrupperType): Faktagrupper {
            return faktagrupper
        }

        override fun lagre(søknad: Søknad): Boolean {
            faktagrupper = Versjon.siste.faktagrupper(søknad, Versjon.FaktagrupperType.Web)
            return true
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
            "opprettet" to LocalDateTime.now(),
            "faktagrupperType" to Versjon.FaktagrupperType.Web.toString()
        )
    )

    private fun nyHendelse(navn: String, hendelse: Map<String, Any>) =
        JsonMessage.newMessage(nyHendelse(navn) + hendelse).toJson()

    private fun nyHendelse(navn: String) = mutableMapOf<String, Any>(
        "@id" to UUID.randomUUID(),
        "@event_name" to navn,
        "@opprettet" to LocalDateTime.now()
    )

    fun besvarFaktum(søknadId: UUID, faktumId: Int, faktumType: String, svar: String) = nyHendelse(
        "faktum_svar",
        mapOf(
            "aktørId" to aktørId,
            "fødselsnummer" to fødselsnummer,
            "avklaringsId" to UUID.randomUUID(),
            "opprettet" to LocalDateTime.now(),
            "faktumId" to faktumId,
            "søknadId" to søknadId,
            "svar" to svar,
            "faktagrupperType" to Versjon.FaktagrupperType.Web.toString(),
            "rolle" to Rolle.søker,
            "faktumType" to faktumType
        )
    )
}
