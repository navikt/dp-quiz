package no.nav.dagpenger.quiz.mediator.db

import kotliquery.Query
import kotliquery.queryOf
import kotliquery.sessionOf
import kotliquery.using
import no.nav.dagpenger.model.faktum.Dokument
import no.nav.dagpenger.model.faktum.Envalg
import no.nav.dagpenger.model.faktum.Fakta
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
import no.nav.dagpenger.quiz.mediator.db.PostgresDataSourceBuilder.dataSource
import no.nav.dagpenger.quiz.mediator.helpers.Postgres
import no.nav.dagpenger.quiz.mediator.helpers.SøknadEksempel1
import no.nav.dagpenger.quiz.mediator.helpers.SøknadEksempel2
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

    private lateinit var originalFakta: Fakta
    private lateinit var rehydrertFakta: Fakta
    private lateinit var faktaRecord: FaktaRecord
    private val faktaversjon = SøknadEksempel1.faktaversjon

    @Test
    fun `ny søknadprosess`() {
        Postgres.withMigratedDb {
            byggOriginalFakta()

            assertRecordCount(1, "fakta")
            assertRecordCount(expectedFaktaCount, "faktum_verdi")
            FaktaRecord().ny(UNG_PERSON_FNR_2018, faktaversjon)
            assertRecordCount(2, "fakta")
            assertRecordCount(expectedFaktaCount * 2, "faktum_verdi")
            lagreHentOgSammenlign()
        }
    }

    @Test
    fun `Slette søknad`() {
        Postgres.withMigratedDb {
            byggOriginalFakta()

            originalFakta.dato(2).besvar(LocalDate.now())
            originalFakta.inntekt(6).besvar(10000.årlig)
            originalFakta.boolsk(10).besvar(true)
            originalFakta.dokument(11).besvar(Dokument(1.januar.atStartOfDay(), "urn:sid:sse"))
            originalFakta.envalg(20).besvar(Envalg("f20.envalg1"))
            originalFakta.flervalg(21).besvar(Flervalg("f21.flervalg1"))
            originalFakta.heltall(22).besvar(123)
            originalFakta.tekst(23).besvar(Tekst("tekst1"))
            originalFakta.periode(24).besvar(Periode(1.januar(), 1.februar()))
            originalFakta.land(25).besvar(Land("SWE"))
            originalFakta.desimaltall(26).besvar(2.5)

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

            originalFakta.dato(2).besvar(LocalDate.now().minusDays(3))
            originalFakta.inntekt(6).besvar(19999.årlig)
            originalFakta.boolsk(10).besvar(false)
            originalFakta.dokument(11).besvar(Dokument(2.januar.atStartOfDay(), "urn:sid:sse"))
            originalFakta.envalg(20).besvar(Envalg("f20.envalg2"))
            originalFakta.flervalg(21).besvar(Flervalg("f21.flervalg2"))
            originalFakta.heltall(22).besvar(456)
            originalFakta.tekst(23).besvar(Tekst("tekst2"))
            originalFakta.periode(24).besvar(Periode(1.mars(), 1.april()))
            originalFakta.land(25).besvar(Land("NOR"))
            originalFakta.desimaltall(26).besvar(1.5, besvarer = "123")

            lagreHentOgSammenlign()

            faktaRecord.slett(originalFakta.uuid)

            assertRecordCount(0, "fakta")
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
        FaktumTable(SøknadEksempel1.prototypeFakta)
        faktaRecord = FaktaRecord()
        val søknadProsess1 = fakta(faktaversjon)
        val søknadProsess2 = fakta(faktaversjon)
        val besvarer = "123"
        søknadProsess1.dato(2).besvar(LocalDate.now(), besvarer = besvarer)
        faktaRecord.lagre(søknadProsess1)
        søknadProsess2.dato(2).besvar(LocalDate.now(), besvarer = besvarer)
        faktaRecord.lagre(søknadProsess2)

        faktaRecord.slett(søknadProsess1.uuid)

        assertRecordCount(1, "besvarer")
    }

    @Test
    fun `Lagring og henting av fakta med kotliquery spesial tegn`() {
        Postgres.withMigratedDb {
            byggOriginalFakta()
            originalFakta.tekst(23).besvar(Tekst("? tekst1 asdfas?"))
            originalFakta.tekst(23).besvar(Tekst(":tekst1"))
            originalFakta.dokument(11).besvar(Dokument(1.januar.atStartOfDay(), "urn:sse:ssi"))
            lagreHentOgSammenlign()
        }
    }

    @Test
    fun `lagring og henting av fakta`() {
        Postgres.withMigratedDb {
            byggOriginalFakta()

            originalFakta.boolsk("1").besvar(true)
            originalFakta.dato(2).besvar(LocalDate.now())
            originalFakta.inntekt(6).besvar(10000.årlig)
            originalFakta.heltall(16).besvar(123)
            originalFakta.dokument(11).besvar(Dokument(1.januar.atStartOfDay(), "urn:sid:sse"))
            originalFakta.envalg(20).besvar(Envalg("f20.envalg1"))
            originalFakta.flervalg(21).besvar(Flervalg("f21.flervalg1"))
            originalFakta.tekst(23).besvar(Tekst("tekst1"))
            originalFakta.periode(24).besvar(Periode(1.januar(), 1.februar()))
            originalFakta.land(25).besvar(Land("NOR"))
            originalFakta.desimaltall(26).besvar(1.5)

            lagreHentOgSammenlign()
        }
    }

    @Test
    fun `lagre nye envalg og flervalg verdier`() {
        Postgres.withMigratedDb {
            byggOriginalFakta()
            originalFakta.envalg(20).besvar(Envalg("f20.envalg1"))
            originalFakta.flervalg(21).besvar(Flervalg("f21.flervalg1"))
            lagreHentOgSammenlign()
            originalFakta.envalg(20).besvar(Envalg("f20.envalg2"))
            originalFakta.flervalg(21).besvar(Flervalg("f21.flervalg1", "f21.flervalg2"))
            lagreHentOgSammenlign()
        }
    }

    @Test
    fun `Nytt svar i fakta burde gjenspeiles i gammel_faktum_verdi`() {
        Postgres.withMigratedDb {
            byggOriginalFakta()

            originalFakta.dato(2).besvar(LocalDate.now())
            originalFakta.inntekt(6).besvar(10000.årlig)
            originalFakta.boolsk(10).besvar(true)
            originalFakta.dokument(11).besvar(Dokument(1.januar.atStartOfDay(), "urn:sid:sse"))
            originalFakta.envalg(20).besvar(Envalg("f20.envalg1"))
            originalFakta.flervalg(21).besvar(Flervalg("f21.flervalg1"))
            originalFakta.heltall(22).besvar(123)
            originalFakta.tekst(23).besvar(Tekst("tekst1"))
            originalFakta.periode(24).besvar(Periode(1.januar(), 1.februar()))
            originalFakta.land(25).besvar(Land("SWE"))
            originalFakta.desimaltall(26).besvar(2.5)

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

            originalFakta.dato(2).besvar(LocalDate.now().minusDays(3))
            originalFakta.inntekt(6).besvar(19999.årlig)
            originalFakta.boolsk(10).besvar(false)
            originalFakta.dokument(11).besvar(Dokument(2.januar.atStartOfDay(), "urn:sid:sse"))
            originalFakta.envalg(20).besvar(Envalg("f20.envalg2"))
            originalFakta.flervalg(21).besvar(Flervalg("f21.flervalg2"))
            originalFakta.heltall(22).besvar(456)
            originalFakta.tekst(23).besvar(Tekst("tekst2"))
            originalFakta.periode(24).besvar(Periode(1.mars(), 1.april()))
            originalFakta.land(25).besvar(Land("NOR"))
            originalFakta.desimaltall(26).besvar(1.5)

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
            byggOriginalFakta()
            assertEquals(expectedFaktaCount, originalFakta.map { it }.size)
            lagreHentOgSammenlign()
            originalFakta = rehydrertFakta

            originalFakta.heltall(15).besvar(3)
            originalFakta.heltall("16.1").besvar(5)
            assertEquals(expectedFaktaCount + 9, originalFakta.map { it }.size)

            lagreHentOgSammenlign()
            assertEquals(expectedFaktaCount + 9, rehydrertFakta.map { it }.size)
        }
    }

    @Test
    fun `redusert template faktum`() {
        Postgres.withMigratedDb {
            byggOriginalFakta()
            assertEquals(expectedFaktaCount, originalFakta.map { it }.size)
            lagreHentOgSammenlign()
            assertRecordCount(0, "gammel_faktum_verdi")
            originalFakta = rehydrertFakta

            originalFakta.heltall(15).besvar(3)
            originalFakta.heltall("16.2").besvar(162)
            originalFakta.heltall("16.3").besvar(163)
            lagreHentOgSammenlign()
            assertRecordCount(0, "gammel_faktum_verdi")
            originalFakta = rehydrertFakta
            originalFakta.heltall(15).besvar(2)
            originalFakta.heltall("16.1").besvar(161)
            originalFakta.heltall("16.2").besvar(1622)
            lagreHentOgSammenlign()
            assertRecordCount(3, "gammel_faktum_verdi")
            assertThrows<IllegalArgumentException> { rehydrertFakta.heltall("16.3") }
        }
    }

    @Test
    fun `utledet faktum med verdi`() {
        Postgres.withMigratedDb {
            byggOriginalFakta()

            originalFakta.dato(3).besvar(3.januar)
            originalFakta.dato(4).besvar(4.januar)
            originalFakta.dato(5).besvar(5.januar)
            lagreHentOgSammenlign()
            assertEquals(5.januar, rehydrertFakta.dato(345).svar())
        }
    }

    @Test
    fun `Maks dato`() {
        Postgres.withMigratedDb {
            byggOriginalFakta()

            originalFakta.dato(3).besvar(LocalDate.MAX)
            lagreHentOgSammenlign()
            assertEquals(LocalDate.MAX, rehydrertFakta.dato(3).svar())
        }
    }

    @Test
    fun `kan lagre fakta besvart med ident`() {
        val ident = "A123456"
        Postgres.withMigratedDb {
            byggOriginalFakta()

            originalFakta.dato(2).besvar(LocalDate.now(), ident)
            originalFakta.inntekt(6).besvar(10000.årlig, ident)
            originalFakta.heltall(16).besvar(123, ident)
            originalFakta.dokument(11).besvar(Dokument(1.januar.atStartOfDay(), "urn:sid:sse"), ident)
            originalFakta.boolsk(1).besvar(true, ident)

            lagreHentOgSammenlign()
        }
    }

    @Test
    fun `Skal kunne lagre pågående perioder, mao sette feltet fom til NULL`() {
        Postgres.withMigratedDb {
            byggOriginalFakta()
            val pågåendePeriode = Periode(17.mai())
            originalFakta.periode(24).besvar(pågåendePeriode)

            lagreHentOgSammenlign()
        }
    }

    @Test
    fun `BUGFIX - dokument lagret flere ganger selvom det ikke er endringer `() {
        Postgres.withMigratedDb {
            byggOriginalFakta()
            originalFakta.dokument(11).besvar(Dokument(1.januar.atStartOfDay(), "urn:sid:sse"))
            lagreHentOgSammenlign()
            lagreHentOgSammenlign()
            assertRecordCount(0, "gammel_faktum_verdi")
            originalFakta.dokument(11).besvar(Dokument(2.januar.atStartOfDay(), "urn:sid:sse"))
            lagreHentOgSammenlign()
            lagreHentOgSammenlign()
            assertRecordCount(1, "gammel_faktum_verdi")
        }
    }

    @Test
    fun `Skal ikke kunne lagre en søknad med samme uuid flere ganger`() {
        Postgres.withMigratedDb {
            val søknadUUId = UUID.randomUUID()
            FaktumTable(SøknadEksempel1.prototypeFakta)
            faktaRecord = FaktaRecord()
            originalFakta =
                fakta(faktaversjon, søknadUUId)
            originalFakta =
                fakta(faktaversjon, søknadUUId)

            originalFakta.dato(2).besvar(LocalDate.now())

            lagreHentOgSammenlign()
        }
    }

    @Test
    fun `Kan migrere`() {
        Postgres.withMigratedDb {
            using(sessionOf(dataSource)) {
                it.run(Query("ALTER SEQUENCE faktum_id_seq INCREMENT 2").asExecute)
            }
            byggOriginalFakta()
            val soknadUUID = originalFakta.uuid
            assertEquals(
                faktaRecord.migrer(soknadUUID, SøknadEksempel1.faktaversjon),
                SøknadEksempel1.faktaversjon,
                "Migrering til samme versjon",
            )

            originalFakta.desimaltall("f26").besvar(9.9)

            FaktumTable(SøknadEksempel2.prototypeFakta)
            val nyProsessVersjon = faktaRecord.migrer(soknadUUID, SøknadEksempel2.faktaversjon)

            assertEquals(SøknadEksempel2.faktaversjon, nyProsessVersjon)

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
                faktaRecord.lagre(this)
            }

            with(faktaRecord.hent(soknadUUID)) {
                assertTrue(desimaltall("f27").erBesvart())
            }
        }
    }

    private fun fakta(faktaversjon: Faktaversjon, søknadUUID: UUID): Fakta {
        return faktaRecord.ny(AvhengigeFaktaTest.UNG_PERSON_FNR_2018, faktaversjon, søknadUUID)
    }

    private fun fakta(faktaversjon: Faktaversjon) = fakta(faktaversjon, UUID.randomUUID())

    private fun lagreHentOgSammenlign() {
        faktaRecord.lagre(originalFakta)
        val uuid = FaktaRecord().opprettede(UNG_PERSON_FNR_2018).toSortedMap().values.first()
        faktaRecord = FaktaRecord()
        rehydrertFakta = faktaRecord.hent(uuid)
        assertJsonEquals(originalFakta.toList(), rehydrertFakta.toList())
        assertDeepEquals(originalFakta, rehydrertFakta)
    }

    private fun byggOriginalFakta() {
        FaktumTable(SøknadEksempel1.prototypeFakta)
        faktaRecord = FaktaRecord()
        originalFakta = fakta(faktaversjon)
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
                queryOf(
                    //language=PostgreSQL
                    """SELECT $kolonnenavn FROM gammel_faktum_verdi WHERE $kolonnenavn IS NOT NULL""",
                ).map {
                    it.string(kolonnenavn)
                }.asSingle,
            )
        }
    }
}
