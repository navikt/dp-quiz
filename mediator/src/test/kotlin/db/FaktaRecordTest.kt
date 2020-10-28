package db

import DataSourceBuilder.dataSource
import helpers.FaktaEksempel.prototypeFakta
import helpers.Postgres
import kotliquery.queryOf
import kotliquery.sessionOf
import kotliquery.using
import no.nav.dagpenger.model.fakta.Inntekt.Companion.årlig
import no.nav.dagpenger.model.fakta.Rolle
import no.nav.dagpenger.model.søknad.Versjon
import no.nav.helse.serde.assertDeepEquals
import org.junit.jupiter.api.Test
import java.time.LocalDate
import kotlin.test.assertEquals

internal class FaktaRecordTest {
    companion object {
        internal const val UNG_PERSON_FNR_2018 = "12020052345"
    }

    @Test
    fun `ny søknad`() {
        Postgres.withMigratedDb {
            FaktumTable(prototypeFakta, 1)
            val originalSøknad = FaktaRecord().ny(UNG_PERSON_FNR_2018, Versjon.Type.Web)
            assertRecordCount(1, "fakta")
            assertRecordCount(21, "faktum_verdi")
            FaktaRecord().ny(UNG_PERSON_FNR_2018, Versjon.Type.Web)
            assertRecordCount(2, "fakta")
            assertRecordCount(42, "faktum_verdi")

            val uuid = FaktaRecord().opprettede(UNG_PERSON_FNR_2018).toSortedMap().values.first()
            val rehydrertSøknad = FaktaRecord().hent(uuid, Versjon.Type.Web)

            //assertDeepEquals(originalSøknad, rehydrertSøknad)
        }
    }

    @Test
    fun `lagring og henting av fakta`() {
        Postgres.withMigratedDb {
            FaktumTable(prototypeFakta, 1)
            val faktaRecord = FaktaRecord()
            val originalSøknad = faktaRecord.ny(UNG_PERSON_FNR_2018, Versjon.Type.Web)

            originalSøknad.ja(1).besvar(true, Rolle.søker)
            originalSøknad.dato(2).besvar(LocalDate.now(), Rolle.søker)
            originalSøknad.inntekt(6).besvar(10000.årlig, Rolle.søker)
            originalSøknad.heltall(16).besvar(123, Rolle.søker)

            faktaRecord.lagre(originalSøknad.fakta)

            val uuid = FaktaRecord().opprettede(UNG_PERSON_FNR_2018).toSortedMap().values.first()
            val rehydrertSøknad = FaktaRecord().hent(uuid, Versjon.Type.Web)
            // assertDeepEquals(originalSøknad, rehydrertSøknad)
        }
    }

    private fun assertRecordCount(recordCount: Int, table: String) {
        assertEquals(
            recordCount,
            using(sessionOf(dataSource)) { session ->
                session.run(
                    queryOf(
                        "SELECT COUNT (*) FROM $table"
                    ).map { it.int(1) }.asSingle
                )
            }
        )
    }
}
