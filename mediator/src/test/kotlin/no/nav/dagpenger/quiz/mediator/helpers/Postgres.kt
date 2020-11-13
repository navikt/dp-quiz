package no.nav.dagpenger.quiz.mediator.helpers

import DataSourceBuilder
import org.testcontainers.containers.PostgreSQLContainer

internal object Postgres {

    val instance by lazy {
        PostgreSQLContainer<Nothing>("postgres:12").apply {
            start()
        }
    }

    fun withMigratedDb(block: () -> Unit) {
        withCleanDb {
            DataSourceBuilder.runMigration()
            block()
        }
    }

    fun withCleanDb(block: () -> Unit) {
        System.setProperty(DataSourceBuilder.DB_JDBC_URL, instance.jdbcUrl)
        System.setProperty(DataSourceBuilder.DB_PASSWORD_KEY, instance.password)
        System.setProperty(DataSourceBuilder.DB_USERNAME_KEY, instance.username)
        DataSourceBuilder.clean().run {
            block()
        }.also {
            System.clearProperty(DataSourceBuilder.DB_URL_KEY)
            System.clearProperty(DataSourceBuilder.DB_PASSWORD_KEY)
            System.clearProperty(DataSourceBuilder.DB_USERNAME_KEY)
        }
    }
}
