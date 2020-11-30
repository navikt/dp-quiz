package no.nav.dagpenger.quiz.mediator.db

import PostgresDataSourceBuilder.dataSource
import kotliquery.queryOf
import kotliquery.sessionOf
import kotliquery.using
import no.nav.dagpenger.model.faktum.Dokument
import no.nav.dagpenger.model.faktum.Identer
import no.nav.dagpenger.model.faktum.Inntekt.Companion.årlig
import no.nav.dagpenger.model.seksjon.Søknadprosess
import no.nav.dagpenger.model.seksjon.Versjon
import no.nav.dagpenger.model.seksjon.Versjon.UserInterfaceType.Web
import no.nav.dagpenger.quiz.mediator.helpers.Postgres
import no.nav.dagpenger.quiz.mediator.helpers.SøknadEksempel1.prototypeFakta1
import no.nav.dagpenger.quiz.mediator.helpers.januar
import no.nav.helse.serde.assertDeepEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class SøknadRecordTest {
    companion object {
        internal val UNG_PERSON_FNR_2018 = Identer.Builder().folkeregisterIdent("12020052345").build()
    }

    private lateinit var originalSøknadprosess: Søknadprosess
    private lateinit var rehydrertSøknadprosess: Søknadprosess
    private lateinit var søknadRecord: SøknadRecord

    @Test
    fun `ny søknadprosess`() {
        Postgres.withMigratedDb {
            byggOriginalSøknadprosess()

            assertRecordCount(1, "soknad")
            assertRecordCount(26, "faktum_verdi")
            SøknadRecord().ny(UNG_PERSON_FNR_2018, Web, 15)
            assertRecordCount(2, "soknad")
            assertRecordCount(52, "faktum_verdi")
            hentFørstFakta()
        }
    }

    @Test
    fun `lagring og henting av fakta`() {
        Postgres.withMigratedDb {
            byggOriginalSøknadprosess()

            originalSøknadprosess.ja(1).besvar(true)
            originalSøknadprosess.dato(2).besvar(LocalDate.now())
            originalSøknadprosess.inntekt(6).besvar(10000.årlig)
            originalSøknadprosess.heltall(16).besvar(123)
            originalSøknadprosess.dokument(11).besvar(Dokument(1.januar.atStartOfDay()))

            hentFørstFakta()
        }
    }

    @Test
    fun `Avhengig faktum reset`() {
        Postgres.withMigratedDb {
            byggOriginalSøknadprosess()

            originalSøknadprosess.dato(2).besvar(2.januar)
            originalSøknadprosess.dato(13).besvar(13.januar)
            originalSøknadprosess.ja(19).besvar(true)
            hentFørstFakta()
            assertRecordCount(3, "gammel_faktum_verdi")
            assertTrue(rehydrertSøknadprosess.ja(19).svar())
            originalSøknadprosess.dato(2).besvar(22.januar)
            hentFørstFakta()
            assertRecordCount(5, "gammel_faktum_verdi")
            assertFalse(rehydrertSøknadprosess.ja(19).erBesvart())
        }
    }

    @Test
    fun `Genererte template faktum`() {
        Postgres.withMigratedDb {
            byggOriginalSøknadprosess()
            assertEquals(26, originalSøknadprosess.søknad.map { it }.size)
            hentFørstFakta()
            originalSøknadprosess = rehydrertSøknadprosess

            originalSøknadprosess.heltall(15).besvar(3)
            originalSøknadprosess.heltall("16.1").besvar(5)
            assertEquals(26 + 9, originalSøknadprosess.søknad.map { it }.size)

            hentFørstFakta()
            assertEquals(26 + 9, rehydrertSøknadprosess.søknad.map { it }.size)
        }
    }

    @Test
    fun `lagrer og rehydrerer valg`() {
        Postgres.withMigratedDb {
            byggOriginalSøknadprosess()
            assertSesjonType(Web)
            originalSøknadprosess.ja(345214).besvar(true)

            hentFørstFakta()
            assertSesjonType(Web)
            assertTrue(rehydrertSøknadprosess.ja(345214).svar())
            assertTrue(rehydrertSøknadprosess.ja(20).svar())

            originalSøknadprosess = rehydrertSøknadprosess
            originalSøknadprosess.ja(345216).besvar(true)

            hentFørstFakta()
            assertSesjonType(Web)
            assertTrue(rehydrertSøknadprosess.ja(345216).svar())
            assertFalse(rehydrertSøknadprosess.ja(20).svar())
        }
    }

    @Test
    fun `redusert template faktum`() {
        Postgres.withMigratedDb {
            byggOriginalSøknadprosess()
            assertEquals(26, originalSøknadprosess.søknad.map { it }.size)
            hentFørstFakta()
            originalSøknadprosess = rehydrertSøknadprosess

            originalSøknadprosess.heltall(15).besvar(3)
            originalSøknadprosess.heltall("16.2").besvar(162)
            originalSøknadprosess.heltall("16.3").besvar(163)
            hentFørstFakta()
            originalSøknadprosess = rehydrertSøknadprosess
            originalSøknadprosess.heltall(15).besvar(2)
            originalSøknadprosess.heltall("16.1").besvar(161)
            originalSøknadprosess.heltall("16.2").besvar(1622)
            hentFørstFakta()
            assertThrows<IllegalArgumentException> { rehydrertSøknadprosess.heltall("16.3") }
        }
    }

    @Test
    fun `utledet faktum med verdi`() {
        Postgres.withMigratedDb {
            byggOriginalSøknadprosess()

            originalSøknadprosess.dato(3).besvar(3.januar)
            originalSøknadprosess.dato(4).besvar(4.januar)
            originalSøknadprosess.dato(5).besvar(5.januar)
            hentFørstFakta()
            assertEquals(5.januar, rehydrertSøknadprosess.dato(345).svar())
        }
    }

    private fun hentFørstFakta(userInterfaceType: Versjon.UserInterfaceType = Web) {
        søknadRecord.lagre(originalSøknadprosess.søknad)
        val uuid = SøknadRecord().opprettede(UNG_PERSON_FNR_2018).toSortedMap().values.first()
        søknadRecord = SøknadRecord()
        rehydrertSøknadprosess = søknadRecord.hent(uuid, userInterfaceType)
        assertDeepEquals(originalSøknadprosess, rehydrertSøknadprosess)
    }

    private fun byggOriginalSøknadprosess() {
        FaktumTable(prototypeFakta1, 15)
        søknadRecord = SøknadRecord()
        originalSøknadprosess = søknadRecord.ny(UNG_PERSON_FNR_2018, Web, 15)
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
