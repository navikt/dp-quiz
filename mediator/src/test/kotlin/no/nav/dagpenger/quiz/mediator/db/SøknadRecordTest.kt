package no.nav.dagpenger.quiz.mediator.db

import kotliquery.queryOf
import kotliquery.sessionOf
import kotliquery.using
import no.nav.dagpenger.model.faktum.Dokument
import no.nav.dagpenger.model.faktum.Identer
import no.nav.dagpenger.model.faktum.Inntekt.Companion.årlig
import no.nav.dagpenger.model.faktum.Prosessversjon
import no.nav.dagpenger.model.faktum.Valg
import no.nav.dagpenger.model.seksjon.Søknadprosess
import no.nav.dagpenger.model.seksjon.Versjon
import no.nav.dagpenger.model.seksjon.Versjon.UserInterfaceType.Web
import no.nav.dagpenger.quiz.mediator.db.PostgresDataSourceBuilder.dataSource
import no.nav.dagpenger.quiz.mediator.helpers.Postgres
import no.nav.dagpenger.quiz.mediator.helpers.SøknadEksempel1
import no.nav.dagpenger.quiz.mediator.helpers.Testprosess
import no.nav.dagpenger.quiz.mediator.helpers.assertDeepEquals
import no.nav.dagpenger.quiz.mediator.helpers.januar
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDate

internal class SøknadRecordTest {
    companion object {
        internal val UNG_PERSON_FNR_2018 = Identer.Builder().folkeregisterIdent("12020052345").build()
        private const val expectedFaktaCount = 23
    }

    private lateinit var originalSøknadprosess: Søknadprosess
    private lateinit var rehydrertSøknadprosess: Søknadprosess
    private lateinit var søknadRecord: SøknadRecord

    @Test
    fun `ny søknadprosess`() {
        Postgres.withMigratedDb {
            byggOriginalSøknadprosess()
            assertSesjonType(Web)

            assertRecordCount(1, "soknad")
            assertRecordCount(expectedFaktaCount, "faktum_verdi")
            SøknadRecord().ny(UNG_PERSON_FNR_2018, Web, Prosessversjon(Testprosess.Test, 888))
            assertRecordCount(2, "soknad")
            assertRecordCount(expectedFaktaCount * 2, "faktum_verdi")
            hentFørsteSøknad()
        }
    }

    @Test
    fun `lagring og henting av fakta`() {
        Postgres.withMigratedDb {
            byggOriginalSøknadprosess()

            originalSøknadprosess.boolsk("1").besvar(true)
            originalSøknadprosess.dato(2).besvar(LocalDate.now())
            originalSøknadprosess.inntekt(6).besvar(10000.årlig)
            originalSøknadprosess.heltall(16).besvar(123)
            originalSøknadprosess.dokument(11).besvar(Dokument(1.januar.atStartOfDay()))
            originalSøknadprosess.valg(20).besvar(Valg("valg1"))
            originalSøknadprosess.desimaltall(21).besvar(200.0)

            hentFørsteSøknad()
        }
    }

    @Test
    fun `Genererte template faktum`() {
        Postgres.withMigratedDb {
            byggOriginalSøknadprosess()
            assertEquals(expectedFaktaCount, originalSøknadprosess.søknad.map { it }.size)
            hentFørsteSøknad()
            originalSøknadprosess = rehydrertSøknadprosess

            originalSøknadprosess.heltall(15).besvar(3)
            originalSøknadprosess.heltall("16.1").besvar(5)
            assertEquals(expectedFaktaCount + 9, originalSøknadprosess.søknad.map { it }.size)

            hentFørsteSøknad()
            assertEquals(expectedFaktaCount + 9, rehydrertSøknadprosess.søknad.map { it }.size)
        }
    }

    @Test
    fun `redusert template faktum`() {
        Postgres.withMigratedDb {
            byggOriginalSøknadprosess()
            assertEquals(expectedFaktaCount, originalSøknadprosess.søknad.map { it }.size)
            hentFørsteSøknad()
            originalSøknadprosess = rehydrertSøknadprosess

            originalSøknadprosess.heltall(15).besvar(3)
            originalSøknadprosess.heltall("16.2").besvar(162)
            originalSøknadprosess.heltall("16.3").besvar(163)
            hentFørsteSøknad()
            originalSøknadprosess = rehydrertSøknadprosess
            originalSøknadprosess.heltall(15).besvar(2)
            originalSøknadprosess.heltall("16.1").besvar(161)
            originalSøknadprosess.heltall("16.2").besvar(1622)
            hentFørsteSøknad()
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
            hentFørsteSøknad()
            assertEquals(5.januar, rehydrertSøknadprosess.dato(345).svar())
        }
    }

    @Test
    fun `valg faktum med verdi`() {
        Postgres.withMigratedDb {
            byggOriginalSøknadprosess()
            originalSøknadprosess.valg(20).besvar(Valg("valg1", "valg2"))
            hentFørsteSøknad()
            assertEquals(Valg("valg1", "valg2"), rehydrertSøknadprosess.valg(20).svar())
        }
    }

    @Test
    fun `Maks dato`() {
        Postgres.withMigratedDb {
            byggOriginalSøknadprosess()

            originalSøknadprosess.dato(3).besvar(LocalDate.MAX)
            hentFørsteSøknad()
            assertEquals(LocalDate.MAX, rehydrertSøknadprosess.dato(3).svar())
        }
    }

    @Test
    fun `kan lagre fakta besvart med ident`() {
        val ident = "A123456"
        Postgres.withMigratedDb {
            byggOriginalSøknadprosess()

            originalSøknadprosess.dato(2).besvar(LocalDate.now(), ident)
            originalSøknadprosess.inntekt(6).besvar(10000.årlig, ident)
            originalSøknadprosess.heltall(16).besvar(123, ident)
            originalSøknadprosess.dokument(11).besvar(Dokument(1.januar.atStartOfDay()), ident)
            originalSøknadprosess.boolsk(1).besvar(true, ident)
            originalSøknadprosess.valg(20).besvar(Valg("valg1"), ident)

            hentFørsteSøknad()
        }
    }

    @Test
    fun `BUG - dokument lagret flere ganger selvom det ikke er endringer `() {
        Postgres.withMigratedDb {
            byggOriginalSøknadprosess()
            originalSøknadprosess.dokument(11).besvar(Dokument(1.januar.atStartOfDay()))
            hentFørsteSøknad()
            hentFørsteSøknad()
            assertRecordCount(1, "gammel_faktum_verdi")
        }
    }

    private fun hentFørsteSøknad(userInterfaceType: Versjon.UserInterfaceType = Web) {
        søknadRecord.lagre(originalSøknadprosess.søknad)
        val uuid = SøknadRecord().opprettede(UNG_PERSON_FNR_2018).toSortedMap().values.first()
        søknadRecord = SøknadRecord()
        rehydrertSøknadprosess = søknadRecord.hent(uuid, userInterfaceType)
        assertDeepEquals(originalSøknadprosess, rehydrertSøknadprosess)
    }

    private fun byggOriginalSøknadprosess() {
        FaktumTable(SøknadEksempel1.prototypeFakta1)
        søknadRecord = SøknadRecord()
        originalSøknadprosess = søknadRecord.ny(UNG_PERSON_FNR_2018, Web, SøknadEksempel1.prosessVersjon)
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
