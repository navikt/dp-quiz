package db

import DataSourceBuilder
import DataSourceBuilder.runMigration
import org.junit.jupiter.api.Test
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.containers.PostgreSQLContainer.POSTGRESQL_PORT
import kotlin.test.assertEquals

internal class PostgresTest {

    internal object PostgresContainer {
        val instance by lazy {
            PostgreSQLContainer<Nothing>("postgres:11.2").apply {
                start()
            }
        }

        fun PostgreSQLContainer<Nothing>.fullJdbcUrl(): String {
            return "jdbc:postgresql://$username:$password@$host:${getMappedPort(POSTGRESQL_PORT)}/$databaseName"
        }
    }

    companion object {
    }

    fun withCleanDb(test: () -> Unit) {
        System.setProperty(DataSourceBuilder.DB_URL_KEY, PostgresContainer.instance.jdbcUrl)
        System.setProperty(DataSourceBuilder.DB_PASSWORD, PostgresContainer.instance.password)
        System.setProperty(DataSourceBuilder.DB_USERNAME, PostgresContainer.instance.username)
        DataSourceBuilder.clean().run {
            test()
        }.also {
            System.clearProperty(DataSourceBuilder.DB_URL_KEY)
            System.clearProperty(DataSourceBuilder.DB_PASSWORD)
            System.clearProperty(DataSourceBuilder.DB_USERNAME)
        }
    }

    @Test
    fun `Migration scripts are applied successfully`() {
        withCleanDb {
            val migrations = runMigration()
            assertEquals(1, migrations)
        }
    }
}
