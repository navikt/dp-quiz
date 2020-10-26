package db

import DataSourceBuilder
import DataSourceBuilder.Companion.runMigration
import com.zaxxer.hikari.HikariDataSource
import org.junit.jupiter.api.Test
import org.testcontainers.containers.PostgreSQLContainer
import kotlin.test.assertEquals

internal class PostgresTest {

    internal object PostgresContainer {
        val instance by lazy {
            PostgreSQLContainer<Nothing>("postgres:11.2").apply {
                start()
            }
        }
    }

    internal object DataSource {
        val instance: HikariDataSource by lazy {
            HikariDataSource().apply {
                username = PostgresContainer.instance.username
                password = PostgresContainer.instance.password
                jdbcUrl = PostgresContainer.instance.jdbcUrl
                connectionTimeout = 1000L
            }
        }
    }

    private fun withCleanDb(test: () -> Unit) = DataSource.instance.also {
        DataSourceBuilder.clean(it)
    }.run { test() }

    @Test
    fun `Migration scripts are applied successfully`() {
        withCleanDb {
            val migrations = runMigration(DataSource.instance)
            assertEquals(1, migrations)
        }
    }
}
