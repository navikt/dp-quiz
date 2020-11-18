package no.nav.dagpenger.quiz.mediator.meldinger

import no.nav.dagpenger.model.seksjon.Versjon
import no.nav.dagpenger.model.seksjon.Versjon.UserInterfaceType.Mobile
import no.nav.dagpenger.quiz.mediator.db.FaktumTable
import no.nav.dagpenger.quiz.mediator.db.SøknadRecord
import no.nav.dagpenger.quiz.mediator.db.SøknadRecordTest
import no.nav.dagpenger.quiz.mediator.helpers.Postgres
import no.nav.dagpenger.quiz.mediator.helpers.SøknadEksempel1
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.UUID
import kotlin.test.assertEquals

internal class HentSeksjonServiceTest {

    @Test
    fun `henter vanlig seksjon`() {
        val rapid = TestRapid()

        Postgres.withMigratedDb {
            FaktumTable(SøknadEksempel1.prototypeFakta1, 1000)
            val søknadRecord = SøknadRecord()
            søknadRecord.ny(SøknadRecordTest.UNG_PERSON_FNR_2018, Versjon.UserInterfaceType.Web)
            val uuid = SøknadRecord().opprettede(SøknadRecordTest.UNG_PERSON_FNR_2018).toSortedMap().values.first()

            HentSeksjonService(rapid)
            rapid.sendTestMessage(hentSeksjonJsonString(uuid))
            assertEquals(1, rapid.inspektør.size)
        }
    }

    @Test
    fun `Henter generert seksjon`() {
        val rapid = TestRapid()

        Postgres.withMigratedDb {
            FaktumTable(SøknadEksempel1.prototypeFakta1, 1000)
            val søknadRecord = SøknadRecord()
            val fakta = søknadRecord.ny(SøknadRecordTest.UNG_PERSON_FNR_2018, Mobile)
            fakta.heltall(15).besvar(3)
            søknadRecord.lagre(fakta.søknad)
            val uuid = SøknadRecord().opprettede(SøknadRecordTest.UNG_PERSON_FNR_2018).toSortedMap().values.first()

            HentSeksjonService(rapid)
            rapid.sendTestMessage(hentSeksjonJsonString(uuid, 1, "template seksjon"))
            assertEquals(1, rapid.inspektør.size)
        }
    }
}

private fun hentSeksjonJsonString(uuid: UUID, indeks: Int = 0, seksjon: String = "seksjon") =
    """{
    "@event_name": "hent_seksjon",
    "@opprettet": "${LocalDateTime.now()}",
    "@id": "${UUID.randomUUID()}",
    "fnr": "12020052345",
    "søknad_uuid": "$uuid",
    "seksjon_navn": "$seksjon",
    "indeks": $indeks
}""".trimMargin()
