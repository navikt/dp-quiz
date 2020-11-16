package no.nav.dagpenger.quiz.mediator.db

import PostgresDataSourceBuilder.dataSource
import kotliquery.queryOf
import kotliquery.sessionOf
import kotliquery.using
import no.nav.dagpenger.model.faktum.Dokument
import no.nav.dagpenger.model.faktum.Inntekt.Companion.årlig
import no.nav.dagpenger.model.seksjon.Søknadprosess
import no.nav.dagpenger.model.seksjon.Versjon
import no.nav.dagpenger.model.seksjon.Versjon.UserInterfaceType.Web
import no.nav.dagpenger.quiz.mediator.helpers.Postgres
import no.nav.dagpenger.quiz.mediator.helpers.SøknadEksempel1.prototypeFakta1
import no.nav.dagpenger.quiz.mediator.helpers.januar
import no.nav.helse.serde.assertDeepEquals
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class SøknadRecordTest {
    companion object {
        internal const val UNG_PERSON_FNR_2018 = "12020052345"
    }

    private lateinit var originalFaktagrupper: Søknadprosess
    private lateinit var rehydrertFaktagrupper: Søknadprosess
    private lateinit var søknadRecord: SøknadRecord

    @Test
    fun `ny søknadprosess`() {
        Postgres.withMigratedDb {
            byggOriginalFaktagrupper()

            assertRecordCount(1, "soknad")
            assertRecordCount(26, "faktum_verdi")
            SøknadRecord().ny(UNG_PERSON_FNR_2018, Web)
            assertRecordCount(2, "soknad")
            assertRecordCount(52, "faktum_verdi")
            hentFørstFakta()
        }
    }

    @Test
    fun `lagring og henting av fakta`() {
        Postgres.withMigratedDb {
            byggOriginalFaktagrupper()

            originalFaktagrupper.ja(1).besvar(true)
            originalFaktagrupper.dato(2).besvar(LocalDate.now())
            originalFaktagrupper.inntekt(6).besvar(10000.årlig)
            originalFaktagrupper.heltall(16).besvar(123)
            originalFaktagrupper.dokument(11).besvar(Dokument(1.januar.atStartOfDay()))

            hentFørstFakta()
        }
    }

    @Test
    fun `Avhengig faktum reset`() {
        Postgres.withMigratedDb {
            byggOriginalFaktagrupper()

            originalFaktagrupper.dato(2).besvar(2.januar)
            originalFaktagrupper.dato(13).besvar(13.januar)
            originalFaktagrupper.ja(19).besvar(true)
            hentFørstFakta()
            assertRecordCount(3, "gammel_faktum_verdi")
            assertTrue(rehydrertFaktagrupper.ja(19).svar())
            originalFaktagrupper.dato(2).besvar(22.januar)
            hentFørstFakta()
            assertRecordCount(5, "gammel_faktum_verdi")
            assertFalse(rehydrertFaktagrupper.ja(19).erBesvart())
        }
    }

    @Test
    fun `Genererte template faktum`() {
        Postgres.withMigratedDb {
            byggOriginalFaktagrupper()
            assertEquals(26, originalFaktagrupper.søknad.map { it }.size)
            hentFørstFakta()
            originalFaktagrupper = rehydrertFaktagrupper

            originalFaktagrupper.heltall(15).besvar(3)
            originalFaktagrupper.heltall("16.1").besvar(5)
            assertEquals(26 + 9, originalFaktagrupper.søknad.map { it }.size)

            hentFørstFakta()
            assertEquals(26 + 9, rehydrertFaktagrupper.søknad.map { it }.size)
        }
    }

    @Test
    fun `lagrer og rehydrerer valg`() {
        Postgres.withMigratedDb {
            byggOriginalFaktagrupper()
            assertSesjonType(Web)
            originalFaktagrupper.ja(345214).besvar(true)

            hentFørstFakta()
            assertSesjonType(Web)
            assertTrue(rehydrertFaktagrupper.ja(345214).svar())
            assertTrue(rehydrertFaktagrupper.ja(20).svar())

            originalFaktagrupper = rehydrertFaktagrupper
            originalFaktagrupper.ja(345216).besvar(true)

            hentFørstFakta()
            assertSesjonType(Web)
            assertTrue(rehydrertFaktagrupper.ja(345216).svar())
            assertFalse(rehydrertFaktagrupper.ja(20).svar())
        }
    }

    @Test
    @Disabled
    fun `redusert template faktum`() {
        Postgres.withMigratedDb {
            byggOriginalFaktagrupper()
            assertEquals(21, originalFaktagrupper.søknad.map { it }.size)
            hentFørstFakta()
            originalFaktagrupper = rehydrertFaktagrupper

            originalFaktagrupper.heltall(15).besvar(3)
            originalFaktagrupper.heltall("16.2").besvar(162)
            originalFaktagrupper.heltall("16.3").besvar(163)
            hentFørstFakta()
            originalFaktagrupper = rehydrertFaktagrupper
            originalFaktagrupper.heltall(15).besvar(2)
            originalFaktagrupper.heltall("16.1").besvar(161)
            originalFaktagrupper.heltall("16.2").besvar(1622)
            hentFørstFakta()
        }
    }

    @Test
    fun `utledet faktum med verdi`() {
        Postgres.withMigratedDb {
            byggOriginalFaktagrupper()

            originalFaktagrupper.dato(3).besvar(3.januar)
            originalFaktagrupper.dato(4).besvar(4.januar)
            originalFaktagrupper.dato(5).besvar(5.januar)
            hentFørstFakta()
            assertEquals(5.januar, rehydrertFaktagrupper.dato(345).svar())
        }
    }

    private fun hentFørstFakta(userInterfaceType: Versjon.UserInterfaceType = Web) {
        søknadRecord.lagre(originalFaktagrupper.søknad)
        val uuid = SøknadRecord().opprettede(UNG_PERSON_FNR_2018).toSortedMap().values.first()
        søknadRecord = SøknadRecord()
        rehydrertFaktagrupper = søknadRecord.hent(uuid, userInterfaceType)
        assertDeepEquals(originalFaktagrupper, rehydrertFaktagrupper)
    }

    private fun byggOriginalFaktagrupper() {
        FaktumTable(prototypeFakta1, 1000)
        søknadRecord = SøknadRecord()
        originalFaktagrupper = søknadRecord.ny(UNG_PERSON_FNR_2018, Web)
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

    private fun assertSesjonType(sesjonType: Versjon.UserInterfaceType) {
        val uuid = SøknadRecord().opprettede(UNG_PERSON_FNR_2018).toSortedMap().values.first()
        assertEquals(
            sesjonType.id,
            using(sessionOf(dataSource)) { session ->
                session.run(
                    queryOf(
                        "SELECT sesjon_type_id FROM soknad WHERE uuid = ?",
                        uuid
                    ).map { it.int(1) }.asSingle
                )
            }
        )
    }
}
