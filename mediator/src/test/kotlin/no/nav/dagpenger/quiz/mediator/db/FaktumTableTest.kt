package no.nav.dagpenger.quiz.mediator.db

import kotliquery.queryOf
import kotliquery.sessionOf
import kotliquery.using
import no.nav.dagpenger.model.faktum.ProsessVersjon
import no.nav.dagpenger.quiz.mediator.db.PostgresDataSourceBuilder.dataSource
import no.nav.dagpenger.quiz.mediator.helpers.Postgres
import no.nav.dagpenger.quiz.mediator.helpers.SøknadEksempel1
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class FaktumTableTest {
    companion object {
        const val expectedFaktumRecordCount = 21
    }
    @Test
    fun `Bygg faktum tabell`() {
        val prosessVersjon = ProsessVersjon("test", 1)

        Postgres.withMigratedDb {
            FaktumTable(SøknadEksempel1.prototypeFakta1, prosessVersjon)
            assertRecordCount(expectedFaktumRecordCount, "faktum")
            assertRecordCount(6, "utledet_faktum")
            assertRecordCount(3, "template_faktum")
            assertRecordCount(5, "avhengig_faktum")
            FaktumTable(SøknadEksempel1.prototypeFakta1, prosessVersjon)
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
            },
            "forventet $recordCount poster i $table"
        )
    }
}
