package no.nav.dagpenger.quiz.mediator.helpers

import no.nav.dagpenger.quiz.mediator.db.PostgresDataSourceBuilder
import org.flywaydb.core.internal.configuration.ConfigUtils
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.containers.PostgreSQLContainer.POSTGRESQL_PORT

internal object Postgres {

    val instance by lazy {
        PostgreSQLContainer<Nothing>("postgres:14.4").apply {
            start()
        }
    }

    fun withMigratedDb(block: () -> Unit) {
        withCleanDb {
            PostgresDataSourceBuilder.runMigration()
            block()
        }
    }

    fun withCleanDb(block: () -> Unit) {
        System.setProperty(ConfigUtils.CLEAN_DISABLED, "false")
        System.setProperty(PostgresDataSourceBuilder.DB_HOST_KEY, instance.host)
        System.setProperty(PostgresDataSourceBuilder.DB_PORT_KEY, instance.getMappedPort(POSTGRESQL_PORT).toString())
        System.setProperty(PostgresDataSourceBuilder.DB_DATABASE_KEY, instance.databaseName)
        System.setProperty(PostgresDataSourceBuilder.DB_PASSWORD_KEY, instance.password)
        System.setProperty(PostgresDataSourceBuilder.DB_USERNAME_KEY, instance.username)
        PostgresDataSourceBuilder.clean().run {
            block()
        }.also {
            System.clearProperty(PostgresDataSourceBuilder.DB_PASSWORD_KEY)
            System.clearProperty(PostgresDataSourceBuilder.DB_USERNAME_KEY)
            System.clearProperty(PostgresDataSourceBuilder.DB_HOST_KEY)
            System.clearProperty(PostgresDataSourceBuilder.DB_PORT_KEY)
            System.clearProperty(PostgresDataSourceBuilder.DB_DATABASE_KEY)
            System.clearProperty(ConfigUtils.CLEAN_DISABLED)
        }
    }
}
