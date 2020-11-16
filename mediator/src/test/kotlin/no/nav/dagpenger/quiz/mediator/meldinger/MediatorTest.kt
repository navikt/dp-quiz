package no.nav.dagpenger.quiz.mediator.meldinger
import no.nav.dagpenger.model.faktum.Dokument
import no.nav.dagpenger.model.faktum.Inntekt.Companion.årlig
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.seksjon.Søknadprosess
import no.nav.dagpenger.model.seksjon.Versjon
import no.nav.dagpenger.quiz.mediator.db.SøknadPersistence
import no.nav.dagpenger.quiz.mediator.helpers.SøknadEksempel
import no.nav.dagpenger.quiz.mediator.helpers.desember
import no.nav.dagpenger.quiz.mediator.helpers.januar
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
        grupperer.søknadprosess = null
    }

    private companion object {
        private val meldingsfabrikk = TestMeldingFactory("fødselsnummer", "aktør")
        private val testRapid = TestRapid()
        private val grupperer = TestLagring()

        init {
            val behovMediator = BehovMediator(testRapid)
            ØnskerRettighetsavklaringerService(grupperer, behovMediator, testRapid)
            FaktumSvarService(grupperer, behovMediator, testRapid)
            SøknadEksempel
        }
    }

    @Test
    internal fun `Start ny søknad, og send første seksjon`() {
        testRapid.sendTestMessage(meldingsfabrikk.ønskerRettighetsavklaring())
        assertEquals(1, testRapid.inspektør.size)
        assertNotNull(grupperer.søknadprosess)
    }

    @Test
    internal fun `ta imot svar`() {
        testRapid.sendTestMessage(meldingsfabrikk.ønskerRettighetsavklaring())
        val uuid = UUID.fromString(testRapid.inspektør.message(0)["søknadUuid"].asText())
        testRapid.sendTestMessage(meldingsfabrikk.besvarFaktum(uuid, 1, "boolean", "true"))
        testRapid.sendTestMessage(meldingsfabrikk.besvarFaktum(uuid, 2, "boolean", "true"))
        testRapid.sendTestMessage(meldingsfabrikk.besvarFaktum(uuid, 3, "heltall", "2"))
        testRapid.sendTestMessage(meldingsfabrikk.besvarFaktum(uuid, 4, "dato", 24.desember.toString()))
        testRapid.sendTestMessage(meldingsfabrikk.besvarFaktum(uuid, 5, "inntekt", 1000.årlig.toString()))
        testRapid.sendTestMessage(meldingsfabrikk.besvarFaktum(uuid, 6, "inntekt", 1050.årlig.toString()))
        testRapid.sendTestMessage(meldingsfabrikk.besvarFaktum(uuid, 7, "dokument", Dokument(1.januar.atStartOfDay(), "https://nav.no")))
        testRapid.sendTestMessage(meldingsfabrikk.besvarFaktum(uuid, 8, "boolean", "true"))
        assertEquals(7, testRapid.inspektør.size)
    }

    private class TestLagring : SøknadPersistence {
        var søknadprosess: Søknadprosess? = null
        private val versjon = 3

        override fun ny(fnr: String, type: Versjon.UserInterfaceType) =
            Versjon.id(versjon).søknadprosess(fnr, type).also { søknadprosess = it }

        override fun hent(uuid: UUID, type: Versjon.UserInterfaceType?) = søknadprosess!!

        override fun lagre(søknad: Søknad): Boolean {
            søknadprosess = Versjon.id(versjon).søknadprosess(søknad, Versjon.UserInterfaceType.Web)
            return true
        }

        override fun opprettede(fnr: String): Map<LocalDateTime, UUID> {
            TODO("Not yet implemented")
        }
    }
}

private class TestMeldingFactory(private val fnr: String, private val aktørId: String) {
    fun ønskerRettighetsavklaring(): String = nyHendelse(
        "ønsker_rettighetsavklaring",
        mapOf(
            "fnr" to fnr,
            "opprettet" to LocalDateTime.now(),
            "faktagrupperType" to Versjon.UserInterfaceType.Web.toString()
        )
    )

    private fun nyHendelse(navn: String, hendelse: Map<String, Any>) =
        JsonMessage.newMessage(nyHendelse(navn) + hendelse).toJson()

    private fun nyHendelse(navn: String) = mutableMapOf<String, Any>(
        "@id" to UUID.randomUUID(),
        "@event_name" to navn,
        "@opprettet" to LocalDateTime.now()
    )

    fun besvarFaktum(søknadUuid: UUID, faktumId: Int, clazz: String, svar: Any) = nyHendelse(
        "faktum_svar",
        mapOf(
            "fnr" to fnr,
            "opprettet" to LocalDateTime.now(),
            "faktumId" to faktumId,
            "søknadUuid" to søknadUuid,
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
            "clazz" to clazz
        )
    )
}
