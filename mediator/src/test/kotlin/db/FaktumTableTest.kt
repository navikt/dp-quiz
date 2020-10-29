package db

import DataSourceBuilder.dataSource
import helpers.FaktaEksempel1.prototypeFakta1
import helpers.Postgres
import kotliquery.queryOf
import kotliquery.sessionOf
import kotliquery.using
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class FaktumTableTest {
    @Test
    fun `Bygg faktum tabell`() {

        Postgres.withMigratedDb {
            FaktumTable(prototypeFakta1, 1)
            assertRecordCount(21, "faktum")
            assertRecordCount(6, "utledet_faktum")
            assertRecordCount(3, "template_faktum")
            assertRecordCount(3, "avhengig_faktum")
            FaktumTable(prototypeFakta1, 1)
            assertRecordCount(21, "faktum")
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
