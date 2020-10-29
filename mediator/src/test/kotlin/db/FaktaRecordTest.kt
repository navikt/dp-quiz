package db

import DataSourceBuilder.dataSource
import helpers.FaktaEksempel.prototypeFakta
import helpers.Postgres
import helpers.januar
import kotliquery.queryOf
import kotliquery.sessionOf
import kotliquery.using
import no.nav.dagpenger.model.fakta.Inntekt.Companion.årlig
import no.nav.dagpenger.model.fakta.Rolle
import no.nav.dagpenger.model.søknad.Søknad
import no.nav.dagpenger.model.søknad.Versjon
import no.nav.helse.serde.assertDeepEquals
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class FaktaRecordTest {
    companion object {
        internal const val UNG_PERSON_FNR_2018 = "12020052345"
    }

    private lateinit var originalSøknad: Søknad
    private lateinit var rehydrertSøknad: Søknad
    private lateinit var faktaRecord: FaktaRecord

    @Test
    fun `ny søknad`() {
        Postgres.withMigratedDb {
            byggOriginalSøknad()

            assertRecordCount(1, "fakta")
            assertRecordCount(21, "faktum_verdi")
            FaktaRecord().ny(UNG_PERSON_FNR_2018, Versjon.Type.Web)
            assertRecordCount(2, "fakta")
            assertRecordCount(42, "faktum_verdi")
            hentFørstFakta()
        }
    }

    @Test
    fun `lagring og henting av fakta`() {
        Postgres.withMigratedDb {
            byggOriginalSøknad()

            originalSøknad.ja(1).besvar(true, Rolle.søker)
            originalSøknad.dato(2).besvar(LocalDate.now(), Rolle.søker)
            originalSøknad.inntekt(6).besvar(10000.årlig, Rolle.søker)
            originalSøknad.heltall(16).besvar(123, Rolle.søker)

            hentFørstFakta()
        }
    }

    @Test
    fun `Avhengig faktum reset`() {
        Postgres.withMigratedDb {
            byggOriginalSøknad()

            originalSøknad.dato(2).besvar(2.januar, Rolle.søker)
            originalSøknad.dato(13).besvar(13.januar, Rolle.søker)
            originalSøknad.ja(19).besvar(true, Rolle.søker)
            hentFørstFakta()
            assertTrue(rehydrertSøknad.ja(19).svar())
            originalSøknad.dato(2).besvar(22.januar, Rolle.søker)
            hentFørstFakta()
            assertFalse(rehydrertSøknad.ja(19).erBesvart())
        }
    }

    @Test
    fun `Genererte template faktum`() {
        Postgres.withMigratedDb {
            byggOriginalSøknad()
            assertEquals(21, originalSøknad.fakta.map { it }.size)
            hentFørstFakta()
            originalSøknad = rehydrertSøknad

            originalSøknad.heltall(15).besvar(3, Rolle.søker)
            originalSøknad.heltall("16.1").besvar(5, Rolle.søker)
            assertEquals(30, originalSøknad.fakta.map { it }.size)

            hentFørstFakta()
            assertEquals(30, rehydrertSøknad.fakta.map { it }.size)
        }
    }

    @Test
    @Disabled
    fun `redusert template faktum`() {
        Postgres.withMigratedDb {
            byggOriginalSøknad()
            assertEquals(21, originalSøknad.fakta.map { it }.size)
            hentFørstFakta()
            originalSøknad = rehydrertSøknad

            originalSøknad.heltall(15).besvar(3, Rolle.søker)
            originalSøknad.heltall("16.2").besvar(162, Rolle.søker)
            originalSøknad.heltall("16.3").besvar(163, Rolle.søker)
            hentFørstFakta()
            originalSøknad = rehydrertSøknad
            originalSøknad.heltall(15).besvar(2, Rolle.søker)
            originalSøknad.heltall("16.1").besvar(161, Rolle.søker)
            originalSøknad.heltall("16.2").besvar(1622, Rolle.søker)
            hentFørstFakta()
        }
    }

    @Test
    fun `utledet faktum med verdi`() {
        Postgres.withMigratedDb {
            byggOriginalSøknad()

            originalSøknad.dato(3).besvar(3.januar, Rolle.søker)
            originalSøknad.dato(4).besvar(4.januar, Rolle.søker)
            originalSøknad.dato(5).besvar(5.januar, Rolle.søker)
            hentFørstFakta()
            assertEquals(5.januar, rehydrertSøknad.dato(345).svar())
        }
    }

    private fun hentFørstFakta() {
        faktaRecord.lagre(originalSøknad.fakta)
        val uuid = FaktaRecord().opprettede(UNG_PERSON_FNR_2018).toSortedMap().values.first()
        faktaRecord = FaktaRecord()
        rehydrertSøknad = faktaRecord.hent(uuid, Versjon.Type.Web)
        assertDeepEquals(originalSøknad, rehydrertSøknad)
    }

    private fun byggOriginalSøknad() {
        FaktumTable(prototypeFakta, 1)
        faktaRecord = FaktaRecord()
        originalSøknad = faktaRecord.ny(UNG_PERSON_FNR_2018, Versjon.Type.Web)
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
