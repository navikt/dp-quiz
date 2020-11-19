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

internal class MeldingMediatorTest {

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
            ØnskerRettighetsavklaringerService(grupperer, testRapid)
            FaktumSvarService(grupperer, testRapid)
            SøknadEksempel
        }
    }

    @Test
    fun `Start ny søknad, og send første seksjon`() {
        testRapid.sendTestMessage(meldingsfabrikk.ønskerRettighetsavklaring())
        assertEquals(1, testRapid.inspektør.size)
        assertNotNull(grupperer.søknadprosess)
    }

    @Test
    fun `ta imot svar`() {
        testRapid.sendTestMessage(meldingsfabrikk.ønskerRettighetsavklaring())
        val uuid = UUID.fromString(testRapid.inspektør.message(0)["søknad_uuid"].asText())
        assertEquals("behov", testRapid.inspektør.message(0)["@event_name"].asText())

        testRapid.sendTestMessage(meldingsfabrikk.besvarFaktum(uuid, FaktumSvar(1, "boolean", "true"), FaktumSvar(2, "boolean", "true")))
        assertEquals("behov", testRapid.inspektør.message(1)["@event_name"].asText())

        testRapid.sendTestMessage(meldingsfabrikk.besvarFaktum(uuid, FaktumSvar(3, "heltall", "2")))
        testRapid.sendTestMessage(meldingsfabrikk.besvarFaktum(uuid, FaktumSvar(4, "dato", 24.desember.toString())))
        testRapid.sendTestMessage(meldingsfabrikk.besvarFaktum(uuid, FaktumSvar(5, "inntekt", 1000.årlig.toString())))
        testRapid.sendTestMessage(meldingsfabrikk.besvarFaktum(uuid, FaktumSvar(6, "inntekt", 1050.årlig.toString())))
        testRapid.sendTestMessage(
            meldingsfabrikk.besvarFaktum(
                uuid,
                FaktumSvar(
                    7,
                    "dokument",
                    Dokument(1.januar.atStartOfDay(), "https://nav.no")
                )
            )
        )
        testRapid.sendTestMessage(meldingsfabrikk.besvarFaktum(uuid, FaktumSvar(8, "boolean", "true")))
        assertEquals(8, testRapid.inspektør.size) // 8 fordi vi får saksbehandlerseksjonene to ganger
    }

    private class TestLagring : SøknadPersistence {
        var søknadprosess: Søknadprosess? = null

        override fun ny(fnr: String, type: Versjon.UserInterfaceType, versjonId: Int) =
            Versjon.id(versjonId).søknadprosess(fnr, type).also { søknadprosess = it }

        override fun hent(uuid: UUID, type: Versjon.UserInterfaceType?) = søknadprosess!!

        override fun lagre(søknad: Søknad): Boolean {
            søknadprosess = Versjon.id(Versjon.siste).søknadprosess(søknad, Versjon.UserInterfaceType.Web)
            return true
        }

        override fun opprettede(fnr: String): Map<LocalDateTime, UUID> {
            TODO("Not yet implemented")
        }
    }
}

private data class FaktumSvar(val faktumId: Int, val clazz: String, val svar: Any)

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

    fun besvarFaktum(søknadUuid: UUID, vararg faktumSvarListe: FaktumSvar) = nyHendelse(
        "faktum_svar",
        mapOf(
            "opprettet" to LocalDateTime.now(),
            "søknad_uuid" to søknadUuid,
            "fakta" to faktumSvarListe.asList().map { faktumSvar ->
                mapOf(
                    "faktumId" to faktumSvar.faktumId,
                    "svar" to when (faktumSvar.svar) {
                        is String -> faktumSvar.svar
                        is Dokument -> faktumSvar.svar.reflection { lastOppTidsstempel, url ->
                            mapOf(
                                "lastOppTidsstempel" to lastOppTidsstempel,
                                "url" to url
                            )
                        }
                        else -> throw IllegalArgumentException("Ustøtta svar-type")
                    },
                    "clazz" to faktumSvar.clazz
                )
            }
        )
    )
}
