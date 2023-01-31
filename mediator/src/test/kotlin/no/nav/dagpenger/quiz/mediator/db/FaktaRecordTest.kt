package no.nav.dagpenger.quiz.mediator.db

import kotliquery.Query
import kotliquery.queryOf
import kotliquery.sessionOf
import kotliquery.using
import no.nav.dagpenger.model.faktum.Dokument
import no.nav.dagpenger.model.faktum.Envalg
import no.nav.dagpenger.model.faktum.Flervalg
import no.nav.dagpenger.model.faktum.Identer
import no.nav.dagpenger.model.faktum.Inntekt.Companion.årlig
import no.nav.dagpenger.model.faktum.Land
import no.nav.dagpenger.model.faktum.Periode
import no.nav.dagpenger.model.faktum.HenvendelsesType
import no.nav.dagpenger.model.faktum.Tekst
import no.nav.dagpenger.model.helpers.april
import no.nav.dagpenger.model.helpers.assertDeepEquals
import no.nav.dagpenger.model.helpers.assertJsonEquals
import no.nav.dagpenger.model.helpers.februar
import no.nav.dagpenger.model.helpers.januar
import no.nav.dagpenger.model.helpers.mai
import no.nav.dagpenger.model.helpers.mars
import no.nav.dagpenger.model.seksjon.Faktagrupper
import no.nav.dagpenger.model.seksjon.Versjon
import no.nav.dagpenger.model.seksjon.Versjon.UserInterfaceType.Web
import no.nav.dagpenger.quiz.mediator.db.PostgresDataSourceBuilder.dataSource
import no.nav.dagpenger.quiz.mediator.helpers.Postgres
import no.nav.dagpenger.quiz.mediator.helpers.SøknadEksempel1
import no.nav.dagpenger.quiz.mediator.helpers.SøknadEksempel2
import no.nav.dagpenger.quiz.mediator.helpers.Testprosess
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDate
import java.util.UUID
import kotlin.test.assertNotNull

internal class FaktaRecordTest {
    companion object {
        internal val UNG_PERSON_FNR_2018 = Identer.Builder().folkeregisterIdent("12020052345").build()
        private const val expectedFaktaCount = 28
    }

    private lateinit var originalFaktagrupper: Faktagrupper
    private lateinit var rehydrertFaktagrupper: Faktagrupper
    private lateinit var søknadRecord: SøknadRecord

    @Test
    fun `ny søknadprosess`() {
        Postgres.withMigratedDb {
            byggOriginalSøknadprosess()
            assertSesjonType(Web)

            assertRecordCount(1, "soknad")
            assertRecordCount(expectedFaktaCount, "faktum_verdi")
            SøknadRecord().ny(UNG_PERSON_FNR_2018, Web, HenvendelsesType(Testprosess.Test, 888))
            assertRecordCount(2, "soknad")
            assertRecordCount(expectedFaktaCount * 2, "faktum_verdi")
            lagreHentOgSammenlign()
        }
    }

    @Test
    fun `Slette søknad`() {
        Postgres.withMigratedDb {
            byggOriginalSøknadprosess()

            originalFaktagrupper.dato(2).besvar(LocalDate.now())
            originalFaktagrupper.inntekt(6).besvar(10000.årlig)
            originalFaktagrupper.boolsk(10).besvar(true)
            originalFaktagrupper.dokument(11).besvar(Dokument(1.januar.atStartOfDay(), "urn:sid:sse"))
            originalFaktagrupper.envalg(20).besvar(Envalg("f20.envalg1"))
            originalFaktagrupper.flervalg(21).besvar(Flervalg("f21.flervalg1"))
            originalFaktagrupper.heltall(22).besvar(123)
            originalFaktagrupper.tekst(23).besvar(Tekst("tekst1"))
            originalFaktagrupper.periode(24).besvar(Periode(1.januar(), 1.februar()))
            originalFaktagrupper.land(25).besvar(Land("SWE"))
            originalFaktagrupper.desimaltall(26).besvar(2.5)

            lagreHentOgSammenlign()

            assertNull(gammelVerdiForKolonnen("dato"))
            assertNull(gammelVerdiForKolonnen("aarlig_inntekt"))
            assertNull(gammelVerdiForKolonnen("boolsk"))
            assertNull(gammelVerdiForKolonnen("dokument_id"))
            assertNull(gammelVerdiForKolonnen("envalg_id"))
            assertNull(gammelVerdiForKolonnen("flervalg_id"))
            assertNull(gammelVerdiForKolonnen("heltall"))
            assertNull(gammelVerdiForKolonnen("tekst"))
            assertNull(gammelVerdiForKolonnen("periode_id"))
            assertNull(gammelVerdiForKolonnen("land"))
            assertNull(gammelVerdiForKolonnen("desimaltall"))

            originalFaktagrupper.dato(2).besvar(LocalDate.now().minusDays(3))
            originalFaktagrupper.inntekt(6).besvar(19999.årlig)
            originalFaktagrupper.boolsk(10).besvar(false)
            originalFaktagrupper.dokument(11).besvar(Dokument(2.januar.atStartOfDay(), "urn:sid:sse"))
            originalFaktagrupper.envalg(20).besvar(Envalg("f20.envalg2"))
            originalFaktagrupper.flervalg(21).besvar(Flervalg("f21.flervalg2"))
            originalFaktagrupper.heltall(22).besvar(456)
            originalFaktagrupper.tekst(23).besvar(Tekst("tekst2"))
            originalFaktagrupper.periode(24).besvar(Periode(1.mars(), 1.april()))
            originalFaktagrupper.land(25).besvar(Land("NOR"))
            originalFaktagrupper.desimaltall(26).besvar(1.5, besvarer = "123")

            lagreHentOgSammenlign()

            søknadRecord.slett(originalFaktagrupper.fakta.uuid)

            assertRecordCount(0, "soknad")
            assertRecordCount(0, "faktum_verdi")
            assertRecordCount(0, "gammel_faktum_verdi")
            assertRecordCount(0, "dokument")
            assertRecordCount(0, "valgte_verdier")
            assertRecordCount(0, "periode")
            // TODO: Flytt dette til batchjobb assertRecordCount(0, "besvarer")
        }
    }

    @Test
    @Disabled("Sletting av besvarer for hver søknad er tungt, det må flyttes til en cronjob")
    fun `Skal kun slette besvarer hvis den ikke refererer til andre søknader`() = Postgres.withMigratedDb {
        FaktumTable(SøknadEksempel1.prototypeFakta1)
        søknadRecord = SøknadRecord()
        val søknadProsess1 = søknadRecord.ny(UNG_PERSON_FNR_2018, Web, SøknadEksempel1.prosessVersjon)
        val søknadProsess2 = søknadRecord.ny(UNG_PERSON_FNR_2018, Web, SøknadEksempel1.prosessVersjon)
        val besvarer = "123"
        søknadProsess1.dato(2).besvar(LocalDate.now(), besvarer = besvarer)
        søknadRecord.lagre(søknadProsess1.fakta)
        søknadProsess2.dato(2).besvar(LocalDate.now(), besvarer = besvarer)
        søknadRecord.lagre(søknadProsess2.fakta)

        søknadRecord.slett(søknadProsess1.fakta.uuid)

        assertRecordCount(1, "besvarer")
    }

    @Test
    fun `Lagring og henting av fakta med kotliquery spesial tegn`() {
        Postgres.withMigratedDb {
            byggOriginalSøknadprosess()
            originalFaktagrupper.tekst(23).besvar(Tekst("? tekst1 asdfas?"))
            originalFaktagrupper.tekst(23).besvar(Tekst(":tekst1"))
            originalFaktagrupper.dokument(11).besvar(Dokument(1.januar.atStartOfDay(), "urn:sse:ssi"))
            lagreHentOgSammenlign()
        }
    }

    @Test
    fun `lagring og henting av fakta`() {
        Postgres.withMigratedDb {
            byggOriginalSøknadprosess()

            originalFaktagrupper.boolsk("1").besvar(true)
            originalFaktagrupper.dato(2).besvar(LocalDate.now())
            originalFaktagrupper.inntekt(6).besvar(10000.årlig)
            originalFaktagrupper.heltall(16).besvar(123)
            originalFaktagrupper.dokument(11).besvar(Dokument(1.januar.atStartOfDay(), "urn:sid:sse"))
            originalFaktagrupper.envalg(20).besvar(Envalg("f20.envalg1"))
            originalFaktagrupper.flervalg(21).besvar(Flervalg("f21.flervalg1"))
            originalFaktagrupper.tekst(23).besvar(Tekst("tekst1"))
            originalFaktagrupper.periode(24).besvar(Periode(1.januar(), 1.februar()))
            originalFaktagrupper.land(25).besvar(Land("NOR"))
            originalFaktagrupper.desimaltall(26).besvar(1.5)

            lagreHentOgSammenlign()
        }
    }

    @Test
    fun `lagre nye envalg og flervalg verdier`() {
        Postgres.withMigratedDb {
            byggOriginalSøknadprosess()
            originalFaktagrupper.envalg(20).besvar(Envalg("f20.envalg1"))
            originalFaktagrupper.flervalg(21).besvar(Flervalg("f21.flervalg1"))
            lagreHentOgSammenlign()
            originalFaktagrupper.envalg(20).besvar(Envalg("f20.envalg2"))
            originalFaktagrupper.flervalg(21).besvar(Flervalg("f21.flervalg1", "f21.flervalg2"))
            lagreHentOgSammenlign()
        }
    }

    @Test
    fun `Nytt svar i fakta burde gjenspeiles i gammel_faktum_verdi`() {
        Postgres.withMigratedDb {
            byggOriginalSøknadprosess()

            originalFaktagrupper.dato(2).besvar(LocalDate.now())
            originalFaktagrupper.inntekt(6).besvar(10000.årlig)
            originalFaktagrupper.boolsk(10).besvar(true)
            originalFaktagrupper.dokument(11).besvar(Dokument(1.januar.atStartOfDay(), "urn:sid:sse"))
            originalFaktagrupper.envalg(20).besvar(Envalg("f20.envalg1"))
            originalFaktagrupper.flervalg(21).besvar(Flervalg("f21.flervalg1"))
            originalFaktagrupper.heltall(22).besvar(123)
            originalFaktagrupper.tekst(23).besvar(Tekst("tekst1"))
            originalFaktagrupper.periode(24).besvar(Periode(1.januar(), 1.februar()))
            originalFaktagrupper.land(25).besvar(Land("SWE"))
            originalFaktagrupper.desimaltall(26).besvar(2.5)

            lagreHentOgSammenlign()

            assertNull(gammelVerdiForKolonnen("dato"))
            assertNull(gammelVerdiForKolonnen("aarlig_inntekt"))
            assertNull(gammelVerdiForKolonnen("boolsk"))
            assertNull(gammelVerdiForKolonnen("dokument_id"))
            assertNull(gammelVerdiForKolonnen("envalg_id"))
            assertNull(gammelVerdiForKolonnen("flervalg_id"))
            assertNull(gammelVerdiForKolonnen("heltall"))
            assertNull(gammelVerdiForKolonnen("tekst"))
            assertNull(gammelVerdiForKolonnen("periode_id"))
            assertNull(gammelVerdiForKolonnen("land"))
            assertNull(gammelVerdiForKolonnen("desimaltall"))

            originalFaktagrupper.dato(2).besvar(LocalDate.now().minusDays(3))
            originalFaktagrupper.inntekt(6).besvar(19999.årlig)
            originalFaktagrupper.boolsk(10).besvar(false)
            originalFaktagrupper.dokument(11).besvar(Dokument(2.januar.atStartOfDay(), "urn:sid:sse"))
            originalFaktagrupper.envalg(20).besvar(Envalg("f20.envalg2"))
            originalFaktagrupper.flervalg(21).besvar(Flervalg("f21.flervalg2"))
            originalFaktagrupper.heltall(22).besvar(456)
            originalFaktagrupper.tekst(23).besvar(Tekst("tekst2"))
            originalFaktagrupper.periode(24).besvar(Periode(1.mars(), 1.april()))
            originalFaktagrupper.land(25).besvar(Land("NOR"))
            originalFaktagrupper.desimaltall(26).besvar(1.5)

            lagreHentOgSammenlign()

            assertNotNull(gammelVerdiForKolonnen("dato"))
            assertNotNull(gammelVerdiForKolonnen("aarlig_inntekt"))
            assertNotNull(gammelVerdiForKolonnen("dokument_id"))
            assertNotNull(gammelVerdiForKolonnen("boolsk"))
            assertNotNull(gammelVerdiForKolonnen("envalg_id"))
            assertNotNull(gammelVerdiForKolonnen("flervalg_id"))
            assertNotNull(gammelVerdiForKolonnen("heltall"))
            assertNotNull(gammelVerdiForKolonnen("tekst"))
            assertNotNull(gammelVerdiForKolonnen("periode_id"))
            assertNotNull(gammelVerdiForKolonnen("land"))
            assertNotNull(gammelVerdiForKolonnen("desimaltall"))
        }
    }

    @Test
    fun `Genererte template faktum`() {
        Postgres.withMigratedDb {
            byggOriginalSøknadprosess()
            assertEquals(expectedFaktaCount, originalFaktagrupper.fakta.map { it }.size)
            lagreHentOgSammenlign()
            originalFaktagrupper = rehydrertFaktagrupper

            originalFaktagrupper.heltall(15).besvar(3)
            originalFaktagrupper.heltall("16.1").besvar(5)
            assertEquals(expectedFaktaCount + 9, originalFaktagrupper.fakta.map { it }.size)

            lagreHentOgSammenlign()
            assertEquals(expectedFaktaCount + 9, rehydrertFaktagrupper.fakta.map { it }.size)
        }
    }

    @Test
    fun `redusert template faktum`() {
        Postgres.withMigratedDb {
            byggOriginalSøknadprosess()
            assertEquals(expectedFaktaCount, originalFaktagrupper.fakta.map { it }.size)
            lagreHentOgSammenlign()
            assertRecordCount(0, "gammel_faktum_verdi")
            originalFaktagrupper = rehydrertFaktagrupper

            originalFaktagrupper.heltall(15).besvar(3)
            originalFaktagrupper.heltall("16.2").besvar(162)
            originalFaktagrupper.heltall("16.3").besvar(163)
            lagreHentOgSammenlign()
            assertRecordCount(0, "gammel_faktum_verdi")
            originalFaktagrupper = rehydrertFaktagrupper
            originalFaktagrupper.heltall(15).besvar(2)
            originalFaktagrupper.heltall("16.1").besvar(161)
            originalFaktagrupper.heltall("16.2").besvar(1622)
            lagreHentOgSammenlign()
            assertRecordCount(3, "gammel_faktum_verdi")
            assertThrows<IllegalArgumentException> { rehydrertFaktagrupper.heltall("16.3") }
        }
    }

    @Test
    fun `utledet faktum med verdi`() {
        Postgres.withMigratedDb {
            byggOriginalSøknadprosess()

            originalFaktagrupper.dato(3).besvar(3.januar)
            originalFaktagrupper.dato(4).besvar(4.januar)
            originalFaktagrupper.dato(5).besvar(5.januar)
            lagreHentOgSammenlign()
            assertEquals(5.januar, rehydrertFaktagrupper.dato(345).svar())
        }
    }

    @Test
    fun `Maks dato`() {
        Postgres.withMigratedDb {
            byggOriginalSøknadprosess()

            originalFaktagrupper.dato(3).besvar(LocalDate.MAX)
            lagreHentOgSammenlign()
            assertEquals(LocalDate.MAX, rehydrertFaktagrupper.dato(3).svar())
        }
    }

    @Test
    fun `kan lagre fakta besvart med ident`() {
        val ident = "A123456"
        Postgres.withMigratedDb {
            byggOriginalSøknadprosess()

            originalFaktagrupper.dato(2).besvar(LocalDate.now(), ident)
            originalFaktagrupper.inntekt(6).besvar(10000.årlig, ident)
            originalFaktagrupper.heltall(16).besvar(123, ident)
            originalFaktagrupper.dokument(11).besvar(Dokument(1.januar.atStartOfDay(), "urn:sid:sse"), ident)
            originalFaktagrupper.boolsk(1).besvar(true, ident)

            lagreHentOgSammenlign()
        }
    }

    @Test
    fun `Skal kunne lagre pågående perioder, mao sette feltet fom til NULL`() {
        Postgres.withMigratedDb {
            byggOriginalSøknadprosess()
            val pågåendePeriode = Periode(17.mai())
            originalFaktagrupper.periode(24).besvar(pågåendePeriode)

            lagreHentOgSammenlign()
        }
    }

    @Test
    fun `BUGFIX - dokument lagret flere ganger selvom det ikke er endringer `() {
        Postgres.withMigratedDb {
            byggOriginalSøknadprosess()
            originalFaktagrupper.dokument(11).besvar(Dokument(1.januar.atStartOfDay(), "urn:sid:sse"))
            lagreHentOgSammenlign()
            lagreHentOgSammenlign()
            assertRecordCount(0, "gammel_faktum_verdi")
            originalFaktagrupper.dokument(11).besvar(Dokument(2.januar.atStartOfDay(), "urn:sid:sse"))
            lagreHentOgSammenlign()
            lagreHentOgSammenlign()
            assertRecordCount(1, "gammel_faktum_verdi")
        }
    }

    @Test
    fun `Skal ikke kunne lagre en søknad med samme uuid flere ganger`() {
        Postgres.withMigratedDb {
            val søknadUUId = UUID.randomUUID()
            FaktumTable(SøknadEksempel1.prototypeFakta1)
            søknadRecord = SøknadRecord()
            originalFaktagrupper =
                søknadRecord.ny(UNG_PERSON_FNR_2018, Web, SøknadEksempel1.prosessVersjon, søknadUUId)
            originalFaktagrupper =
                søknadRecord.ny(UNG_PERSON_FNR_2018, Web, SøknadEksempel1.prosessVersjon, søknadUUId)

            originalFaktagrupper.dato(2).besvar(LocalDate.now())

            lagreHentOgSammenlign()
        }
    }

    @Test
    fun `Kan migrere`() {
        Postgres.withMigratedDb {
            using(sessionOf(dataSource)) {
                it.run(Query("ALTER SEQUENCE faktum_id_seq INCREMENT 2").asExecute)
            }
            byggOriginalSøknadprosess()
            val soknadUUID = originalFaktagrupper.fakta.uuid
            assertEquals(
                søknadRecord.migrer(soknadUUID, SøknadEksempel1.prosessVersjon),
                SøknadEksempel1.prosessVersjon,
                "Migrering til samme versjon",
            )

            originalFaktagrupper.desimaltall("f26").besvar(9.9)

            SøknadEksempel2.v2
            FaktumTable(SøknadEksempel2.prototypeFakta)
            val nyProsessVersjon = søknadRecord.migrer(soknadUUID, SøknadEksempel2.prosessVersjon)

            assertEquals(SøknadEksempel2.prosessVersjon, nyProsessVersjon)

            with(søknadRecord.hent(soknadUUID)) {
                assertFalse(heltall("f26").erBesvart())
                assertFalse(desimaltall("f27").erBesvart())

                heltall("f26").besvar(12)
                desimaltall("f27").besvar(1.3)
                envalg("f28").besvar(Envalg("f28.valg1"))
                tekst("f20").besvar(Tekst("Foo"))

                assertThrows<IllegalArgumentException> {
                    // Slettet og finnes ikke lenger
                    tekst("f23").besvar(Tekst("Foo"))
                }

                assertTrue(desimaltall("f27").erBesvart())
                søknadRecord.lagre(this.fakta)
            }

            with(søknadRecord.hent(soknadUUID)) {
                assertTrue(desimaltall("f27").erBesvart())
            }
        }
    }

    private fun lagreHentOgSammenlign(userInterfaceType: Versjon.UserInterfaceType = Web) {
        søknadRecord.lagre(originalFaktagrupper.fakta)
        val uuid = SøknadRecord().opprettede(UNG_PERSON_FNR_2018).toSortedMap().values.first()
        søknadRecord = SøknadRecord()
        rehydrertFaktagrupper = søknadRecord.hent(uuid, userInterfaceType)
        assertJsonEquals(originalFaktagrupper, rehydrertFaktagrupper)
        assertDeepEquals(originalFaktagrupper, rehydrertFaktagrupper)
    }

    private fun byggOriginalSøknadprosess() {
        FaktumTable(SøknadEksempel1.prototypeFakta1)
        søknadRecord = SøknadRecord()
        originalFaktagrupper = søknadRecord.ny(UNG_PERSON_FNR_2018, Web, SøknadEksempel1.prosessVersjon)
    }

    private fun assertRecordCount(recordCount: Int, table: String) {
        assertEquals(
            recordCount,
            using(sessionOf(dataSource)) { session ->
                session.run(
                    queryOf(
                        "SELECT COUNT (*) FROM $table",
                    ).map { it.int(1) }.asSingle,
                )
            },
            "Forventet $recordCount i tabell $table",
        )
    }

    private fun gammelVerdiForKolonnen(kolonnenavn: String): Any? {
        return using(sessionOf(dataSource)) { session ->
            session.run(
                queryOf( //language=PostgreSQL
                    """SELECT $kolonnenavn FROM gammel_faktum_verdi WHERE $kolonnenavn IS NOT NULL""",
                ).map {
                    it.string(kolonnenavn)
                }.asSingle,
            )
        }
    }

    private fun assertSesjonType(sesjonType: Versjon.UserInterfaceType) {
        val uuid = SøknadRecord().opprettede(UNG_PERSON_FNR_2018).toSortedMap().values.first()
        assertEquals(
            sesjonType.id,
            using(sessionOf(dataSource)) { session ->
                session.run(
                    queryOf(
                        "SELECT sesjon_type_id FROM soknad WHERE uuid = ?",
                        uuid,
                    ).map { it.int(1) }.asSingle,
                )
            },
        )
    }
}
