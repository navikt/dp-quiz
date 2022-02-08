package no.nav.dagpenger.quiz.mediator.db

import kotliquery.queryOf
import kotliquery.sessionOf
import kotliquery.using
import no.nav.dagpenger.model.faktum.Envalg
import no.nav.dagpenger.model.faktum.Flervalg
import no.nav.dagpenger.model.faktum.Valg
import no.nav.dagpenger.quiz.mediator.db.PostgresDataSourceBuilder.dataSource
import no.nav.dagpenger.quiz.mediator.helpers.Postgres
import no.nav.dagpenger.quiz.mediator.helpers.SøknadEksempel1
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class FaktumTableTest {
    companion object {
        const val expectedFaktumRecordCount = 27
    }

    @Test
    fun `Bygg faktum tabell`() {
        Postgres.withMigratedDb {
            FaktumTable(SøknadEksempel1.prototypeFakta1)
            assertRecordCount(expectedFaktumRecordCount, "faktum")
            assertRecordCount(6, "utledet_faktum")
            assertRecordCount(3, "template_faktum")
            assertRecordCount(5, "avhengig_faktum")
            assertRecordCount(2, "faktum_gyldige_valg")
            assertGyldigeValg(Envalg("f20.envalg1", "f20.envalg2"), 20)
            assertGyldigeValg(Flervalg("f21.flervalg1", "f21.flervalg2", "f21.flervalg3"), 21)
            FaktumTable(SøknadEksempel1.prototypeFakta1)
            assertRecordCount(expectedFaktumRecordCount, "faktum")
        }
    }

    private fun assertGyldigeValg(valg: Valg, faktumRootId: Int) {
        val verdier: Array<String>? = using(sessionOf(dataSource)) { session ->
            session.run(
                queryOf( //language=PostgreSQL
                    "SELECT verdier FROM faktum_gyldige_valg LEFT JOIN faktum ON faktum.id = faktum_gyldige_valg.faktum_id WHERE faktum.root_id = ?",
                    faktumRootId
                ).map { it.array<String>(1) }.asSingle
            )
        }
        assertEquals(
            valg.joinToString { it },
            verdier?.joinToString { it },
            "forventet $valg men var $verdier "
        )
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
