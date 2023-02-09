package no.nav.dagpenger.quiz.mediator.db

import kotliquery.Query
import kotliquery.queryOf
import kotliquery.sessionOf
import kotliquery.using
import no.nav.dagpenger.model.faktum.Dokument
import no.nav.dagpenger.model.faktum.Envalg
import no.nav.dagpenger.model.faktum.Faktaversjon
import no.nav.dagpenger.model.faktum.Flervalg
import no.nav.dagpenger.model.faktum.Identer
import no.nav.dagpenger.model.faktum.Inntekt.Companion.årlig
import no.nav.dagpenger.model.faktum.Land
import no.nav.dagpenger.model.faktum.Periode
import no.nav.dagpenger.model.faktum.Tekst
import no.nav.dagpenger.model.helpers.april
import no.nav.dagpenger.model.helpers.assertDeepEquals
import no.nav.dagpenger.model.helpers.assertJsonEquals
import no.nav.dagpenger.model.helpers.februar
import no.nav.dagpenger.model.helpers.januar
import no.nav.dagpenger.model.helpers.mai
import no.nav.dagpenger.model.helpers.mars
import no.nav.dagpenger.model.seksjon.Utredningsprosess
import no.nav.dagpenger.model.seksjon.Versjon
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

    private lateinit var originalUtredningsprosess: Utredningsprosess
    private lateinit var rehydrertUtredningsprosess: Utredningsprosess
    private lateinit var faktaRecord: FaktaRecord

    @Test
    fun `ny søknadprosess`() {
        Postgres.withMigratedDb {
            byggOriginalSøknadprosess()

            assertRecordCount(1, "soknad")
            assertRecordCount(expectedFaktaCount, "faktum_verdi")
            FaktaRecord().ny(UNG_PERSON_FNR_2018, Faktaversjon(Testprosess.Test, 888))
            assertRecordCount(2, "soknad")
            assertRecordCount(expectedFaktaCount * 2, "faktum_verdi")
            lagreHentOgSammenlign()
        }
    }

    @Test
    fun `Slette søknad`() {
        Postgres.withMigratedDb {
            byggOriginalSøknadprosess()

            originalUtredningsprosess.dato(2).besvar(LocalDate.now())
            originalUtredningsprosess.inntekt(6).besvar(10000.årlig)
            originalUtredningsprosess.boolsk(10).besvar(true)
            originalUtredningsprosess.dokument(11).besvar(Dokument(1.januar.atStartOfDay(), "urn:sid:sse"))
            originalUtredningsprosess.envalg(20).besvar(Envalg("f20.envalg1"))
            originalUtredningsprosess.flervalg(21).besvar(Flervalg("f21.flervalg1"))
            originalUtredningsprosess.heltall(22).besvar(123)
            originalUtredningsprosess.tekst(23).besvar(Tekst("tekst1"))
            originalUtredningsprosess.periode(24).besvar(Periode(1.januar(), 1.februar()))
            originalUtredningsprosess.land(25).besvar(Land("SWE"))
            originalUtredningsprosess.desimaltall(26).besvar(2.5)

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

            originalUtredningsprosess.dato(2).besvar(LocalDate.now().minusDays(3))
            originalUtredningsprosess.inntekt(6).besvar(19999.årlig)
            originalUtredningsprosess.boolsk(10).besvar(false)
            originalUtredningsprosess.dokument(11).besvar(Dokument(2.januar.atStartOfDay(), "urn:sid:sse"))
            originalUtredningsprosess.envalg(20).besvar(Envalg("f20.envalg2"))
            originalUtredningsprosess.flervalg(21).besvar(Flervalg("f21.flervalg2"))
            originalUtredningsprosess.heltall(22).besvar(456)
            originalUtredningsprosess.tekst(23).besvar(Tekst("tekst2"))
            originalUtredningsprosess.periode(24).besvar(Periode(1.mars(), 1.april()))
            originalUtredningsprosess.land(25).besvar(Land("NOR"))
            originalUtredningsprosess.desimaltall(26).besvar(1.5, besvarer = "123")

            lagreHentOgSammenlign()

            faktaRecord.slett(originalUtredningsprosess.fakta.uuid)

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
        faktaRecord = FaktaRecord()
        val søknadProsess1 = utredningsprosess(SøknadEksempel1.prosessVersjon)
        val søknadProsess2 = utredningsprosess(SøknadEksempel1.prosessVersjon)
        val besvarer = "123"
        søknadProsess1.dato(2).besvar(LocalDate.now(), besvarer = besvarer)
        faktaRecord.lagre(søknadProsess1.fakta)
        søknadProsess2.dato(2).besvar(LocalDate.now(), besvarer = besvarer)
        faktaRecord.lagre(søknadProsess2.fakta)

        faktaRecord.slett(søknadProsess1.fakta.uuid)

        assertRecordCount(1, "besvarer")
    }

    @Test
    fun `Lagring og henting av fakta med kotliquery spesial tegn`() {
        Postgres.withMigratedDb {
            byggOriginalSøknadprosess()
            originalUtredningsprosess.tekst(23).besvar(Tekst("? tekst1 asdfas?"))
            originalUtredningsprosess.tekst(23).besvar(Tekst(":tekst1"))
            originalUtredningsprosess.dokument(11).besvar(Dokument(1.januar.atStartOfDay(), "urn:sse:ssi"))
            lagreHentOgSammenlign()
        }
    }

    @Test
    fun `lagring og henting av fakta`() {
        Postgres.withMigratedDb {
            byggOriginalSøknadprosess()

            originalUtredningsprosess.boolsk("1").besvar(true)
            originalUtredningsprosess.dato(2).besvar(LocalDate.now())
            originalUtredningsprosess.inntekt(6).besvar(10000.årlig)
            originalUtredningsprosess.heltall(16).besvar(123)
            originalUtredningsprosess.dokument(11).besvar(Dokument(1.januar.atStartOfDay(), "urn:sid:sse"))
            originalUtredningsprosess.envalg(20).besvar(Envalg("f20.envalg1"))
            originalUtredningsprosess.flervalg(21).besvar(Flervalg("f21.flervalg1"))
            originalUtredningsprosess.tekst(23).besvar(Tekst("tekst1"))
            originalUtredningsprosess.periode(24).besvar(Periode(1.januar(), 1.februar()))
            originalUtredningsprosess.land(25).besvar(Land("NOR"))
            originalUtredningsprosess.desimaltall(26).besvar(1.5)

            lagreHentOgSammenlign()
        }
    }

    @Test
    fun `lagre nye envalg og flervalg verdier`() {
        Postgres.withMigratedDb {
            byggOriginalSøknadprosess()
            originalUtredningsprosess.envalg(20).besvar(Envalg("f20.envalg1"))
            originalUtredningsprosess.flervalg(21).besvar(Flervalg("f21.flervalg1"))
            lagreHentOgSammenlign()
            originalUtredningsprosess.envalg(20).besvar(Envalg("f20.envalg2"))
            originalUtredningsprosess.flervalg(21).besvar(Flervalg("f21.flervalg1", "f21.flervalg2"))
            lagreHentOgSammenlign()
        }
    }

    @Test
    fun `Nytt svar i fakta burde gjenspeiles i gammel_faktum_verdi`() {
        Postgres.withMigratedDb {
            byggOriginalSøknadprosess()

            originalUtredningsprosess.dato(2).besvar(LocalDate.now())
            originalUtredningsprosess.inntekt(6).besvar(10000.årlig)
            originalUtredningsprosess.boolsk(10).besvar(true)
            originalUtredningsprosess.dokument(11).besvar(Dokument(1.januar.atStartOfDay(), "urn:sid:sse"))
            originalUtredningsprosess.envalg(20).besvar(Envalg("f20.envalg1"))
            originalUtredningsprosess.flervalg(21).besvar(Flervalg("f21.flervalg1"))
            originalUtredningsprosess.heltall(22).besvar(123)
            originalUtredningsprosess.tekst(23).besvar(Tekst("tekst1"))
            originalUtredningsprosess.periode(24).besvar(Periode(1.januar(), 1.februar()))
            originalUtredningsprosess.land(25).besvar(Land("SWE"))
            originalUtredningsprosess.desimaltall(26).besvar(2.5)

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

            originalUtredningsprosess.dato(2).besvar(LocalDate.now().minusDays(3))
            originalUtredningsprosess.inntekt(6).besvar(19999.årlig)
            originalUtredningsprosess.boolsk(10).besvar(false)
            originalUtredningsprosess.dokument(11).besvar(Dokument(2.januar.atStartOfDay(), "urn:sid:sse"))
            originalUtredningsprosess.envalg(20).besvar(Envalg("f20.envalg2"))
            originalUtredningsprosess.flervalg(21).besvar(Flervalg("f21.flervalg2"))
            originalUtredningsprosess.heltall(22).besvar(456)
            originalUtredningsprosess.tekst(23).besvar(Tekst("tekst2"))
            originalUtredningsprosess.periode(24).besvar(Periode(1.mars(), 1.april()))
            originalUtredningsprosess.land(25).besvar(Land("NOR"))
            originalUtredningsprosess.desimaltall(26).besvar(1.5)

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
            assertEquals(expectedFaktaCount, originalUtredningsprosess.fakta.map { it }.size)
            lagreHentOgSammenlign()
            originalUtredningsprosess = rehydrertUtredningsprosess

            originalUtredningsprosess.heltall(15).besvar(3)
            originalUtredningsprosess.heltall("16.1").besvar(5)
            assertEquals(expectedFaktaCount + 9, originalUtredningsprosess.fakta.map { it }.size)

            lagreHentOgSammenlign()
            assertEquals(expectedFaktaCount + 9, rehydrertUtredningsprosess.fakta.map { it }.size)
        }
    }

    @Test
    fun `redusert template faktum`() {
        Postgres.withMigratedDb {
            byggOriginalSøknadprosess()
            assertEquals(expectedFaktaCount, originalUtredningsprosess.fakta.map { it }.size)
            lagreHentOgSammenlign()
            assertRecordCount(0, "gammel_faktum_verdi")
            originalUtredningsprosess = rehydrertUtredningsprosess

            originalUtredningsprosess.heltall(15).besvar(3)
            originalUtredningsprosess.heltall("16.2").besvar(162)
            originalUtredningsprosess.heltall("16.3").besvar(163)
            lagreHentOgSammenlign()
            assertRecordCount(0, "gammel_faktum_verdi")
            originalUtredningsprosess = rehydrertUtredningsprosess
            originalUtredningsprosess.heltall(15).besvar(2)
            originalUtredningsprosess.heltall("16.1").besvar(161)
            originalUtredningsprosess.heltall("16.2").besvar(1622)
            lagreHentOgSammenlign()
            assertRecordCount(3, "gammel_faktum_verdi")
            assertThrows<IllegalArgumentException> { rehydrertUtredningsprosess.heltall("16.3") }
        }
    }

    @Test
    fun `utledet faktum med verdi`() {
        Postgres.withMigratedDb {
            byggOriginalSøknadprosess()

            originalUtredningsprosess.dato(3).besvar(3.januar)
            originalUtredningsprosess.dato(4).besvar(4.januar)
            originalUtredningsprosess.dato(5).besvar(5.januar)
            lagreHentOgSammenlign()
            assertEquals(5.januar, rehydrertUtredningsprosess.dato(345).svar())
        }
    }

    @Test
    fun `Maks dato`() {
        Postgres.withMigratedDb {
            byggOriginalSøknadprosess()

            originalUtredningsprosess.dato(3).besvar(LocalDate.MAX)
            lagreHentOgSammenlign()
            assertEquals(LocalDate.MAX, rehydrertUtredningsprosess.dato(3).svar())
        }
    }

    @Test
    fun `kan lagre fakta besvart med ident`() {
        val ident = "A123456"
        Postgres.withMigratedDb {
            byggOriginalSøknadprosess()

            originalUtredningsprosess.dato(2).besvar(LocalDate.now(), ident)
            originalUtredningsprosess.inntekt(6).besvar(10000.årlig, ident)
            originalUtredningsprosess.heltall(16).besvar(123, ident)
            originalUtredningsprosess.dokument(11).besvar(Dokument(1.januar.atStartOfDay(), "urn:sid:sse"), ident)
            originalUtredningsprosess.boolsk(1).besvar(true, ident)

            lagreHentOgSammenlign()
        }
    }

    @Test
    fun `Skal kunne lagre pågående perioder, mao sette feltet fom til NULL`() {
        Postgres.withMigratedDb {
            byggOriginalSøknadprosess()
            val pågåendePeriode = Periode(17.mai())
            originalUtredningsprosess.periode(24).besvar(pågåendePeriode)

            lagreHentOgSammenlign()
        }
    }

    @Test
    fun `BUGFIX - dokument lagret flere ganger selvom det ikke er endringer `() {
        Postgres.withMigratedDb {
            byggOriginalSøknadprosess()
            originalUtredningsprosess.dokument(11).besvar(Dokument(1.januar.atStartOfDay(), "urn:sid:sse"))
            lagreHentOgSammenlign()
            lagreHentOgSammenlign()
            assertRecordCount(0, "gammel_faktum_verdi")
            originalUtredningsprosess.dokument(11).besvar(Dokument(2.januar.atStartOfDay(), "urn:sid:sse"))
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
            faktaRecord = FaktaRecord()
            originalUtredningsprosess =
                utredningsprosess(SøknadEksempel1.prosessVersjon, søknadUUId)
            originalUtredningsprosess =
                utredningsprosess(SøknadEksempel1.prosessVersjon, søknadUUId)

            originalUtredningsprosess.dato(2).besvar(LocalDate.now())

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
            val soknadUUID = originalUtredningsprosess.fakta.uuid
            assertEquals(
                faktaRecord.migrer(soknadUUID, SøknadEksempel1.prosessVersjon),
                SøknadEksempel1.prosessVersjon,
                "Migrering til samme versjon",
            )

            originalUtredningsprosess.desimaltall("f26").besvar(9.9)

            SøknadEksempel2.v2
            FaktumTable(SøknadEksempel2.prototypeFakta)
            val nyProsessVersjon = faktaRecord.migrer(soknadUUID, SøknadEksempel2.prosessVersjon)

            assertEquals(SøknadEksempel2.prosessVersjon, nyProsessVersjon)

            with(faktaRecord.hent(soknadUUID)) {
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
                faktaRecord.lagre(this.fakta)
            }

            with(faktaRecord.hent(soknadUUID)) {
                assertTrue(desimaltall("f27").erBesvart())
            }
        }
    }

    private fun utredningsprosess(prosessVersjon: Faktaversjon, søknadUUID: UUID): Utredningsprosess {
        val fakta = faktaRecord.ny(AvhengigeFaktaTest.UNG_PERSON_FNR_2018, prosessVersjon, søknadUUID)
        return Versjon.id(prosessVersjon).utredningsprosess(fakta)
    }
    private fun utredningsprosess(prosessVersjon: Faktaversjon) = utredningsprosess(prosessVersjon, UUID.randomUUID())

    private fun lagreHentOgSammenlign() {
        faktaRecord.lagre(originalUtredningsprosess.fakta)
        val uuid = FaktaRecord().opprettede(UNG_PERSON_FNR_2018).toSortedMap().values.first()
        faktaRecord = FaktaRecord()
        rehydrertUtredningsprosess = faktaRecord.hent(uuid)
        assertJsonEquals(originalUtredningsprosess, rehydrertUtredningsprosess)
        assertDeepEquals(originalUtredningsprosess, rehydrertUtredningsprosess)
    }

    private fun byggOriginalSøknadprosess() {
        FaktumTable(SøknadEksempel1.prototypeFakta1)
        faktaRecord = FaktaRecord()
        originalUtredningsprosess = utredningsprosess(SøknadEksempel1.prosessVersjon)
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
