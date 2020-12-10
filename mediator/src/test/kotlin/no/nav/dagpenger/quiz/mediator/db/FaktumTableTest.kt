package no.nav.dagpenger.quiz.mediator.db

import PostgresDataSourceBuilder.dataSource
import kotliquery.queryOf
import kotliquery.sessionOf
import kotliquery.using
import no.nav.dagpenger.quiz.mediator.helpers.Postgres
import no.nav.dagpenger.quiz.mediator.helpers.SøknadEksempel1.Companion.søknadEksempel1
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class FaktumTableTest {
    companion object {
        const val expectedFaktumRecordCount = 21
    }
    @Test
    fun `Bygg faktum tabell`() {
        Postgres.withMigratedDb {
            FaktumTable(søknadEksempel1.prototypeFakta1, 1)
            assertRecordCount(expectedFaktumRecordCount, "faktum")
            assertRecordCount(6, "utledet_faktum")
            assertRecordCount(3, "template_faktum")
            assertRecordCount(4, "avhengig_faktum")
            FaktumTable(søknadEksempel1.prototypeFakta1, 1)
            assertRecordCount(expectedFaktumRecordCount, "faktum")
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
