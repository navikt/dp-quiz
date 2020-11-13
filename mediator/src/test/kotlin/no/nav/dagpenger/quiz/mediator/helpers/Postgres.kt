package no.nav.dagpenger.quiz.mediator.helpers

import PostgresDataSourceBuilder
import org.testcontainers.containers.PostgreSQLContainer

internal object Postgres {

    val instance by lazy {
        PostgreSQLContainer<Nothing>("postgres:12").apply {
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
        System.setProperty(PostgresDataSourceBuilder.DB_URL_KEY, instance.jdbcUrl)
        System.setProperty(PostgresDataSourceBuilder.DB_PASSWORD_KEY, instance.password)
        System.setProperty(PostgresDataSourceBuilder.DB_USERNAME_KEY, instance.username)
        PostgresDataSourceBuilder.clean().run {
            block()
        }.also {
            System.clearProperty(PostgresDataSourceBuilder.DB_URL_KEY)
            System.clearProperty(PostgresDataSourceBuilder.DB_PASSWORD_KEY)
            System.clearProperty(PostgresDataSourceBuilder.DB_USERNAME_KEY)
        }
    }
}
