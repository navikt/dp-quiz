package db

import DataSourceBuilder.dataSource
import helpers.FaktaEksempel1.prototypeFakta1
import helpers.Postgres
import helpers.januar
import kotliquery.queryOf
import kotliquery.sessionOf
import kotliquery.using
import no.nav.dagpenger.model.fakta.Dokument
import no.nav.dagpenger.model.fakta.Inntekt.Companion.årlig
import no.nav.dagpenger.model.fakta.Rolle
import no.nav.dagpenger.model.faktagrupper.Faktagrupper
import no.nav.dagpenger.model.faktagrupper.Versjon
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

    private lateinit var originalFaktagrupper: Faktagrupper
    private lateinit var rehydrertFaktagrupper: Faktagrupper
    private lateinit var faktaRecord: FaktaRecord

    @Test
    fun `ny faktagrupper`() {
        Postgres.withMigratedDb {
            byggOriginalFaktaGrupper()

            assertRecordCount(1, "fakta")
            assertRecordCount(21, "faktum_verdi")
            FaktaRecord().ny(UNG_PERSON_FNR_2018, Versjon.FaktagrupperType.Web)
            assertRecordCount(2, "fakta")
            assertRecordCount(42, "faktum_verdi")
            hentFørstFakta()
        }
    }

    @Test
    fun `lagring og henting av fakta`() {
        Postgres.withMigratedDb {
            byggOriginalFaktaGrupper()

            originalFaktagrupper.ja(1).besvar(true, Rolle.søker)
            originalFaktagrupper.dato(2).besvar(LocalDate.now(), Rolle.søker)
            originalFaktagrupper.inntekt(6).besvar(10000.årlig, Rolle.søker)
            originalFaktagrupper.heltall(16).besvar(123, Rolle.søker)
            originalFaktagrupper.dokument(11).besvar(Dokument(1.januar.atStartOfDay()), Rolle.søker)

            hentFørstFakta()
        }
    }

    @Test
    fun `Avhengig faktum reset`() {
        Postgres.withMigratedDb {
            byggOriginalFaktaGrupper()

            originalFaktagrupper.dato(2).besvar(2.januar, Rolle.søker)
            originalFaktagrupper.dato(13).besvar(13.januar, Rolle.søker)
            originalFaktagrupper.ja(19).besvar(true, Rolle.søker)
            hentFørstFakta()
            assertRecordCount(3, "gammel_faktum_verdi")
            assertTrue(rehydrertFaktagrupper.ja(19).svar())
            originalFaktagrupper.dato(2).besvar(22.januar, Rolle.søker)
            hentFørstFakta()
            assertRecordCount(5, "gammel_faktum_verdi")
            assertFalse(rehydrertFaktagrupper.ja(19).erBesvart())
        }
    }

    @Test
    fun `Genererte template faktum`() {
        Postgres.withMigratedDb {
            byggOriginalFaktaGrupper()
            assertEquals(21, originalFaktagrupper.fakta.map { it }.size)
            hentFørstFakta()
            originalFaktagrupper = rehydrertFaktagrupper

            originalFaktagrupper.heltall(15).besvar(3, Rolle.søker)
            originalFaktagrupper.heltall("16.1").besvar(5, Rolle.søker)
            assertEquals(30, originalFaktagrupper.fakta.map { it }.size)

            hentFørstFakta()
            assertEquals(30, rehydrertFaktagrupper.fakta.map { it }.size)
        }
    }

    @Test
    @Disabled
    fun `redusert template faktum`() {
        Postgres.withMigratedDb {
            byggOriginalFaktaGrupper()
            assertEquals(21, originalFaktagrupper.fakta.map { it }.size)
            hentFørstFakta()
            originalFaktagrupper = rehydrertFaktagrupper

            originalFaktagrupper.heltall(15).besvar(3, Rolle.søker)
            originalFaktagrupper.heltall("16.2").besvar(162, Rolle.søker)
            originalFaktagrupper.heltall("16.3").besvar(163, Rolle.søker)
            hentFørstFakta()
            originalFaktagrupper = rehydrertFaktagrupper
            originalFaktagrupper.heltall(15).besvar(2, Rolle.søker)
            originalFaktagrupper.heltall("16.1").besvar(161, Rolle.søker)
            originalFaktagrupper.heltall("16.2").besvar(1622, Rolle.søker)
            hentFørstFakta()
        }
    }

    @Test
    fun `utledet faktum med verdi`() {
        Postgres.withMigratedDb {
            byggOriginalFaktaGrupper()

            originalFaktagrupper.dato(3).besvar(3.januar, Rolle.søker)
            originalFaktagrupper.dato(4).besvar(4.januar, Rolle.søker)
            originalFaktagrupper.dato(5).besvar(5.januar, Rolle.søker)
            hentFørstFakta()
            assertEquals(5.januar, rehydrertFaktagrupper.dato(345).svar())
        }
    }

    private fun hentFørstFakta() {
        faktaRecord.lagre(originalFaktagrupper.fakta)
        val uuid = FaktaRecord().opprettede(UNG_PERSON_FNR_2018).toSortedMap().values.first()
        faktaRecord = FaktaRecord()
        rehydrertFaktagrupper = faktaRecord.hent(uuid, Versjon.FaktagrupperType.Web)
        assertDeepEquals(originalFaktagrupper, rehydrertFaktagrupper)
    }

    private fun byggOriginalFaktaGrupper() {
        FaktumTable(prototypeFakta1, 1)
        faktaRecord = FaktaRecord()
        originalFaktagrupper = faktaRecord.ny(UNG_PERSON_FNR_2018, Versjon.FaktagrupperType.Web)
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
