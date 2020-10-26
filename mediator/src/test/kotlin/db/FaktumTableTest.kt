package db

import DataSourceBuilder.dataSource
import helpers.FaktaEksempel.prototypeFakta
import kotliquery.queryOf
import kotliquery.sessionOf
import kotliquery.using
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class FaktumTableTest {
    @Test
    fun `Bygg faktum tabell`() {
        System.setProperty(DataSourceBuilder.DB_URL_KEY, PostgresTest.PostgresContainer.instance.jdbcUrl)
        System.setProperty(DataSourceBuilder.DB_PASSWORD, PostgresTest.PostgresContainer.instance.password)
        System.setProperty(DataSourceBuilder.DB_USERNAME, PostgresTest.PostgresContainer.instance.username)

        DataSourceBuilder.runMigration()

        FaktumTable(prototypeFakta, 1)
        assertRecordCount(21, "faktum")
        assertRecordCount(6, "utledet_faktum")
        assertRecordCount(3, "template_faktum")
        FaktumTable(prototypeFakta, 1)
        assertRecordCount(21, "faktum")
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
