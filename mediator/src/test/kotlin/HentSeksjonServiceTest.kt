import db.FaktumTable
import db.SøknadRecord
import db.SøknadRecordTest
import helpers.Postgres
import helpers.SøknadEksempel1
import meldinger.HentSeksjonService
import no.nav.dagpenger.model.faktagrupper.Versjon
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.UUID
import kotlin.test.assertEquals

internal class HentSeksjonServiceTest {

    @Test
    fun ` henter seksjon`() {
        val rapid = TestRapid()

        Postgres.withMigratedDb {
            FaktumTable(SøknadEksempel1.prototypeFakta1, 1000)
            val søknadRecord = SøknadRecord()
            søknadRecord.ny(SøknadRecordTest.UNG_PERSON_FNR_2018, Versjon.FaktagrupperType.Web)
            val uuid = SøknadRecord().opprettede(SøknadRecordTest.UNG_PERSON_FNR_2018).toSortedMap().values.first()

            HentSeksjonService(rapid)
            rapid.sendTestMessage(hentSeksjonJsonString(uuid))
            assertEquals(1, rapid.inspektør.size)
        }
    }
}

private fun hentSeksjonJsonString(uuid: UUID) =
    """{
    "@event_name": "hent_seksjon",
    "@opprettet": "${LocalDateTime.now()}",
    "@id": "${UUID.randomUUID()}",
    "fnr": "121212555555",
    "soknad_uuid": "$uuid",
    "seksjon_navn": "seksjon"
}""".trimMargin()
