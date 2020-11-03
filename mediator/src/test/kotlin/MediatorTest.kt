
import db.SøknadPersistence
import helpers.SøknadEksempel
import helpers.desember
import helpers.januar
import io.mockk.mockk
import no.nav.dagpenger.model.faktagrupper.Faktagrupper
import no.nav.dagpenger.model.faktagrupper.Versjon
import no.nav.dagpenger.model.faktum.Dokument
import no.nav.dagpenger.model.faktum.Inntekt.Companion.årlig
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.faktum.Rolle.nav
import no.nav.dagpenger.model.faktum.Rolle.saksbehandler
import no.nav.dagpenger.model.faktum.Rolle.søker
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.UUID

class MeldingMediatorTest {

    @BeforeEach
    internal fun reset() {
        testRapid.reset()
        grupperer.faktagrupper = null
    }

    private companion object {
        private val meldingsfabrikk = TestMeldingFactory("fødselsnummer", "aktør")
        private val testRapid = TestRapid()
        private val grupperer = TestLagring()
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
    internal fun `Start ny søknad, og send første seksjon`() {
        testRapid.sendTestMessage(meldingsfabrikk.ønskerRettighetsavklaring())
        assertEquals(1, testRapid.inspektør.size)
        assertNotNull(grupperer.faktagrupper)
    }

    @Test
    internal fun `ta imot svar`() {
        testRapid.sendTestMessage(meldingsfabrikk.ønskerRettighetsavklaring())
        val uuid = UUID.fromString(testRapid.inspektør.message(0)["søknadId"].asText())
        testRapid.sendTestMessage(meldingsfabrikk.besvarFaktum(uuid, 1, "boolean", "true", søker))
        testRapid.sendTestMessage(meldingsfabrikk.besvarFaktum(uuid, 2, "boolean", "true", søker))
        testRapid.sendTestMessage(meldingsfabrikk.besvarFaktum(uuid, 3, "heltall", "2", søker))
        testRapid.sendTestMessage(meldingsfabrikk.besvarFaktum(uuid, 4, "dato", 24.desember.toString(), søker))
        testRapid.sendTestMessage(meldingsfabrikk.besvarFaktum(uuid, 5, "inntekt", 1000.årlig.toString(), nav))
        testRapid.sendTestMessage(meldingsfabrikk.besvarFaktum(uuid, 6, "inntekt", 1050.årlig.toString(), nav))
        testRapid.sendTestMessage(meldingsfabrikk.besvarFaktum(uuid, 7, "dokument", Dokument(1.januar.atStartOfDay(), "https://nav.no"), søker))
        testRapid.sendTestMessage(meldingsfabrikk.besvarFaktum(uuid, 8, "boolean", "true", saksbehandler))
        assertEquals(7, testRapid.inspektør.size)
    }

    private class TestLagring : SøknadPersistence {
        var faktagrupper: Faktagrupper? = null

        override fun ny(fnr: String, type: Versjon.FaktagrupperType) =
            Versjon.siste.faktagrupper(fnr, type).also { faktagrupper = it }

        override fun hent(uuid: UUID, type: Versjon.FaktagrupperType) = faktagrupper!!

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

    fun besvarFaktum(søknadId: UUID, faktumId: Int, clazz: String, svar: Any, rolle: Rolle) = nyHendelse(
        "faktum_svar",
        mapOf(
            "aktørId" to aktørId,
            "fødselsnummer" to fødselsnummer,
            "avklaringsId" to UUID.randomUUID(),
            "opprettet" to LocalDateTime.now(),
            "faktumId" to faktumId,
            "søknadId" to søknadId,
            "svar" to when (svar) {
                is String -> svar
                is Dokument -> svar.reflection { lastOppTidsstempel, url ->
                    mapOf(
                        "lastOppTidsstempel" to lastOppTidsstempel,
                        "url" to url
                    )
                }
                else -> throw IllegalArgumentException("Ustøtta svar-type")
            },
            "faktagrupperType" to Versjon.FaktagrupperType.Web.toString(),
            "rolle" to rolle,
            "clazz" to clazz
        )
    )
}
