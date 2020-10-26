package db

import DataSourceBuilder.runMigration
import helpers.Postgres.withCleanDb
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class PostgresMigrationTest {

    @Test
    fun `Migration scripts are applied successfully`() {
        withCleanDb {
            val migrations = runMigration()
            assertEquals(1, migrations)
        }
    }
}
