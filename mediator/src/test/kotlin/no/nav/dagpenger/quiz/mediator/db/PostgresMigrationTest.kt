package no.nav.dagpenger.quiz.mediator.db

import no.nav.dagpenger.quiz.mediator.db.PostgresDataSourceBuilder.runMigration
import no.nav.dagpenger.quiz.mediator.helpers.Postgres.withCleanDb
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class PostgresMigrationTest {

    @Test
    fun `Migration scripts are applied successfully`() {
        withCleanDb {
            val migrations = runMigration()
            assertEquals(29, migrations)
        }
    }
}
