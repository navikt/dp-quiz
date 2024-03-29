package no.nav.dagpenger.quiz.mediator.meldinger

import io.mockk.mockk
import no.nav.dagpenger.model.faktum.Dokument
import no.nav.dagpenger.model.faktum.Inntekt.Companion.årlig
import no.nav.dagpenger.model.helpers.desember
import no.nav.dagpenger.model.helpers.januar
import no.nav.dagpenger.quiz.mediator.db.ResultatPersistence
import no.nav.dagpenger.quiz.mediator.helpers.SøknadEksempel
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.UUID

internal class MediatorTest {
    @BeforeEach
    internal fun reset() {
        testRapid.reset()
        repository.reset()
    }

    private companion object {
        private val meldingsfabrikk = TestMeldingFactory("fødselsnummer", "aktør")
        private val testRapid = TestRapid()
        private val repository = ProsessRepositoryFake(SøknadEksempel.prosesstype, SøknadEksempel.faktaversjon)
        private val resultatPersistence = mockk<ResultatPersistence>(relaxed = true)

        init {
            AvslagPåMinsteinntektService(
                repository,
                testRapid,
            )
            FaktumSvarService(repository, resultatPersistence, testRapid)
        }
    }

    @Test
    fun `ta imot svar`() {
        testRapid.sendTestMessage(meldingsfabrikk.nySøknadMelding())
        val uuid = UUID.fromString(testRapid.inspektør.message(0)["søknad_uuid"].asText())
        assertEquals("faktum_svar", testRapid.inspektør.message(0)["@event_name"].asText())

        testRapid.sendTestMessage(
            meldingsfabrikk.besvarFaktum(
                uuid,
                FaktumSvar(1, "boolean", "true"),
                FaktumSvar(2, "boolean", "true"),
            ),
        )
        assertEquals("faktum_svar", testRapid.inspektør.field(1, "@event_name").asText())
        assertEquals(true, repository.prosess!!.id(1).svar())

        testRapid.sendTestMessage(meldingsfabrikk.besvarFaktum(uuid, FaktumSvar(3, "int", "2")))
        testRapid.sendTestMessage(
            meldingsfabrikk.besvarFaktum(
                uuid,
                FaktumSvar(4, "localdate", 24.desember.toString()),
            ),
        )
        testRapid.sendTestMessage(meldingsfabrikk.besvarFaktum(uuid, FaktumSvar(5, "inntekt", 1000.årlig.toString())))
        testRapid.sendTestMessage(meldingsfabrikk.besvarFaktum(uuid, FaktumSvar(6, "inntekt", 1050.årlig.toString())))
        testRapid.sendTestMessage(
            meldingsfabrikk.besvarFaktum(
                uuid,
                FaktumSvar(
                    7,
                    "dokument",
                    Dokument(1.januar.atStartOfDay(), "urn:nav:somethingg"),
                ),
            ),
        )
        testRapid.sendTestMessage(meldingsfabrikk.besvarFaktum(uuid, FaktumSvar(8, "boolean", "true")))
        assertEquals(8, testRapid.inspektør.size)
        assertEquals("prosess_resultat", testRapid.inspektør.field(7, "@event_name").asText())
    }

    @Test
    fun `at søknaden ikke lastes unødvendig av meldinger uten svar`() {
        testRapid.sendTestMessage(meldingsfabrikk.nySøknadMelding())
        val uuid = UUID.fromString(testRapid.inspektør.message(0)["søknad_uuid"].asText())
        assertEquals(1, testRapid.inspektør.size)

        testRapid.sendTestMessage(
            meldingsfabrikk.besvarFaktum(
                uuid,
                FaktumSvar(1, "boolean", null),
            ),
        )
        assertEquals(1, testRapid.inspektør.size)
        assertEquals(0, repository.hentet, "Faktum uten svar laster ikke søknaden")

        testRapid.sendTestMessage(
            meldingsfabrikk.besvarFaktum(
                uuid,
                FaktumSvar(2, "boolean", "true"),
            ),
        )
        assertEquals(2, testRapid.inspektør.size)
        assertEquals(1, repository.hentet, "Faktum med et svar laster søknaden")

        testRapid.sendTestMessage(
            meldingsfabrikk.besvarFaktum(
                uuid,
                FaktumSvar(1, "boolean", null),
                FaktumSvar(2, "boolean", "true"),
            ),
        )
        assertEquals(3, testRapid.inspektør.size)
        assertEquals(2, repository.hentet, "Fakta hvor minst ett faktum har svar laster søknaden")

        testRapid.sendTestMessage(
            meldingsfabrikk.besvarFaktumMedNull(
                uuid,
                FaktumSvar(1, "boolean", null),
            ),
        )
        assertEquals(4, testRapid.inspektør.size)
        assertEquals(3, repository.hentet, "Faktum med null som svar laster søknaden")
    }
}

private data class FaktumSvar(val faktumId: Int, val type: String, val svar: Any?)

private class TestMeldingFactory(private val fnr: String, private val aktørId: String) {
    fun nySøknadMelding(): String = nyHendelse(
        "innsending_ferdigstilt",
        mapOf(
            "fødselsnummer" to fnr,
            "aktørId" to aktørId,
            "type" to "NySøknad",
            "journalpostId" to "493389306",
            "søknadsData" to mapOf("søknad_uuid" to "mf68etellerannet"),
        ),
    )

    private fun nyHendelse(navn: String, hendelse: Map<String, Any>) =
        JsonMessage.newMessage(nyHendelse(navn) + hendelse).toJson()

    private fun nyHendelse(navn: String) = mutableMapOf<String, Any>(
        "@id" to UUID.randomUUID(),
        "@event_name" to navn,
        "@opprettet" to LocalDateTime.now(),
    )

    fun besvarFaktum(søknadUuid: UUID, vararg faktumSvarListe: FaktumSvar) = nyHendelse(
        "faktum_svar",
        mapOf(
            "opprettet" to LocalDateTime.now(),
            "søknad_uuid" to søknadUuid,
            "fakta" to faktumSvarListe.asList().map { faktumSvar ->
                mapOf(
                    "id" to faktumSvar.faktumId,
                    "type" to faktumSvar.type,
                ).let { fakta ->
                    fakta + faktumSvar.svar?.let {
                        mapOf(
                            "svar" to when (faktumSvar.svar) {
                                is String -> faktumSvar.svar
                                is Dokument -> faktumSvar.svar.reflection { lastOppTidsstempel, urn: String ->
                                    mapOf(
                                        "lastOppTidsstempel" to lastOppTidsstempel,
                                        "urn" to urn,
                                    )
                                }

                                else -> throw IllegalArgumentException("Ustøtta svar-type")
                            },
                        )
                    }.orEmpty()
                }
            },
        ),
    )

    fun besvarFaktumMedNull(søknadUuid: UUID, vararg faktumSvarListe: FaktumSvar) = nyHendelse(
        "faktum_svar",
        mapOf(
            "opprettet" to LocalDateTime.now(),
            "søknad_uuid" to søknadUuid,
            "fakta" to faktumSvarListe.asList().map { faktumSvar ->
                mapOf(
                    "id" to faktumSvar.faktumId,
                    "type" to faktumSvar.type,
                ).let { fakta ->
                    fakta + mapOf(
                        "svar" to null,
                    )
                }
            },
        ),
    )
}
