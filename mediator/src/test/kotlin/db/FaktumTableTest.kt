package db

import DataSourceBuilder.dataSource
import kotliquery.queryOf
import kotliquery.sessionOf
import kotliquery.using
import org.junit.jupiter.api.Test
import soknad.Prototype
import kotlin.test.assertEquals

internal class FaktumTableTest {
    @Test
    fun `Bygg faktum tabell`() {
        System.setProperty(DataSourceBuilder.DB_URL_KEY, PostgresTest.PostgresContainer.instance.jdbcUrl)
        System.setProperty(DataSourceBuilder.DB_PASSWORD, PostgresTest.PostgresContainer.instance.password)
        System.setProperty(DataSourceBuilder.DB_USERNAME, PostgresTest.PostgresContainer.instance.username)

        DataSourceBuilder.runMigration()

        Prototype()
        assertRecordCount(18)
        Prototype()
        assertRecordCount(18)
    }

    private fun assertRecordCount(recordCount: Int) {
        assertEquals(
            recordCount,
            using(sessionOf(dataSource)) { session ->
                session.run(
                    queryOf(
                        "SELECT COUNT (versjon_id) FROM faktum"
                    ).map { it.int(1) }.asSingle
                )
            }
        )
    }
}
