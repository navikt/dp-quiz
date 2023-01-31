package no.nav.dagpenger.quiz.mediator.db

import kotliquery.queryOf
import kotliquery.sessionOf
import kotliquery.using
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.boolsk
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.dato
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.desimaltall
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.dokument
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.envalg
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.flervalg
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.heltall
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.inntekt
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.land
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.periode
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.tekst
import no.nav.dagpenger.model.factory.UtledetFaktumFactory.Companion.maks
import no.nav.dagpenger.model.faktum.Dokument
import no.nav.dagpenger.model.faktum.Envalg
import no.nav.dagpenger.model.faktum.Flervalg
import no.nav.dagpenger.model.faktum.Identer
import no.nav.dagpenger.model.faktum.Inntekt.Companion.årlig
import no.nav.dagpenger.model.faktum.Land
import no.nav.dagpenger.model.faktum.Periode
import no.nav.dagpenger.model.faktum.Prosessversjon
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.faktum.Tekst
import no.nav.dagpenger.model.helpers.assertDeepEquals
import no.nav.dagpenger.model.helpers.januar
import no.nav.dagpenger.model.regel.er
import no.nav.dagpenger.model.seksjon.Faktagrupper
import no.nav.dagpenger.model.seksjon.Seksjon
import no.nav.dagpenger.model.seksjon.Versjon
import no.nav.dagpenger.model.seksjon.Versjon.UserInterfaceType.Web
import no.nav.dagpenger.quiz.mediator.db.PostgresDataSourceBuilder.dataSource
import no.nav.dagpenger.quiz.mediator.helpers.Postgres
import no.nav.dagpenger.quiz.mediator.helpers.Testprosess
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class AvhengigeFaktaTest {
    companion object {
        internal val UNG_PERSON_FNR_2018 = Identer.Builder().folkeregisterIdent("12020052345").build()
    }

    private lateinit var originalFaktagrupper: Faktagrupper
    private lateinit var rehydrertFaktagrupper: Faktagrupper
    private lateinit var søknadRecord: SøknadRecord

    @Test
    fun `Avhengig faktum reset`() {
        val prosessVersjon = Prosessversjon(Testprosess.Test, 634)
        Postgres.withMigratedDb {
            val prototypeFakta = Søknad(
                prosessVersjon,
                boolsk faktum "f1" id 19 avhengerAv 2 og 13,
                dato faktum "f2" id 2,
                dato faktum "f3" id 13,

            )
            Versjon.Bygger(
                prototypeFakta,
                prototypeFakta boolsk 19 er true,
                mapOf(
                    Web to Faktagrupper(
                        Seksjon(
                            "seksjon",
                            Rolle.nav,
                            *(prototypeFakta.map { it }.toTypedArray())
                        )
                    )
                )
            ).registrer()
            FaktumTable(prototypeFakta)
            søknadRecord = SøknadRecord()
            originalFaktagrupper = søknadRecord.ny(SøknadRecordTest.UNG_PERSON_FNR_2018, Web, prosessVersjon)

            originalFaktagrupper.dato(2).besvar(2.januar)
            originalFaktagrupper.dato(13).besvar(13.januar)
            originalFaktagrupper.boolsk(19).besvar(true)
            lagreHentOgSammenlign()
            assertRecordCount(0, "gammel_faktum_verdi")
            assertTrue(rehydrertFaktagrupper.boolsk(19).svar())
            originalFaktagrupper.dato(2).besvar(22.januar)
            lagreHentOgSammenlign()
            assertRecordCount(2, "gammel_faktum_verdi")
            assertFalse(rehydrertFaktagrupper.boolsk(19).erBesvart())
        }
    }

    @Test
    fun `Avhengig faktum rehydreres`() {
        val prosessVersjon = Prosessversjon(Testprosess.Test, 635)

        Postgres.withMigratedDb {
            val prototypeFakta = Søknad(
                prosessVersjon,
                boolsk faktum "f1" id 1 avhengerAv 4,
                boolsk faktum "f2" id 2,
                boolsk faktum "f3" id 3 avhengerAv 1,
                boolsk faktum "f4" id 4 avhengerAv 5,
                boolsk faktum "f5" id 5,
            )
            Versjon.Bygger(
                prototypeFakta,
                prototypeFakta boolsk 1 er true,
                mapOf(
                    Web to Faktagrupper(
                        Seksjon(
                            "seksjon",
                            Rolle.nav,
                            *(prototypeFakta.map { it }.toTypedArray())
                        )
                    )
                )
            ).registrer()
            FaktumTable(prototypeFakta)

            søknadRecord = SøknadRecord()
            originalFaktagrupper = søknadRecord.ny(SøknadRecordTest.UNG_PERSON_FNR_2018, Web, prosessVersjon)

            originalFaktagrupper.boolsk(2).besvar(true)
            originalFaktagrupper.boolsk(5).besvar(true)
            originalFaktagrupper.boolsk(4).besvar(true)
            originalFaktagrupper.boolsk(1).besvar(true)
            originalFaktagrupper.boolsk(3).besvar(true)

            assertEquals(5, originalFaktagrupper.søknad.count { it.erBesvart() })
            søknadRecord.lagre(originalFaktagrupper.søknad)

            rehydrertFaktagrupper = søknadRecord.hent(originalFaktagrupper.søknad.uuid)
            assertEquals(5, rehydrertFaktagrupper.søknad.count { it.erBesvart() })
        }
    }

    @Test
    fun `Avhengig av utledet faktum rehydreres`() {
        val prosessVersjon = Prosessversjon(Testprosess.Test, 636)

        Postgres.withMigratedDb {
            val prototypeFakta = Søknad(
                prosessVersjon,
                boolsk faktum "f1" id 1 avhengerAv 4,
                dato faktum "f2" id 2,
                dato faktum "f3" id 3,
                maks dato "f4" av 2 og 3 id 4,
                boolsk faktum "f1" id 5 avhengerAv 4,
            )
            Versjon.Bygger(
                prototypeFakta,
                prototypeFakta boolsk 1 er true,
                mapOf(
                    Web to Faktagrupper(
                        Seksjon(
                            "seksjon",
                            Rolle.nav,
                            *(prototypeFakta.map { it }.toTypedArray())
                        )
                    )
                )
            ).registrer()
            FaktumTable(prototypeFakta)

            søknadRecord = SøknadRecord()
            originalFaktagrupper = søknadRecord.ny(SøknadRecordTest.UNG_PERSON_FNR_2018, Web, prosessVersjon)

            originalFaktagrupper.dato(2).besvar(1.januar)
            originalFaktagrupper.dato(3).besvar(10.januar)
            originalFaktagrupper.boolsk(1).besvar(true)
            originalFaktagrupper.boolsk(5).besvar(true)

            assertEquals(5, originalFaktagrupper.søknad.count { it.erBesvart() })
            søknadRecord.lagre(originalFaktagrupper.søknad)

            rehydrertFaktagrupper = søknadRecord.hent(originalFaktagrupper.søknad.uuid)
            assertEquals(5, rehydrertFaktagrupper.søknad.count { it.erBesvart() })
        }
    }

    @Test
    fun `Alle avhengige faktumtyper resettes`() {
        val prosessVersjon = Prosessversjon(Testprosess.Test, 637)
        Postgres.withMigratedDb {
            val prototypeFakta = Søknad(
                prosessVersjon,
                boolsk faktum "f1" id 1,
                dato faktum "f2" id 2 avhengerAv 1,
                boolsk faktum "f3" id 3 avhengerAv 1,
                heltall faktum "f4" id 4 avhengerAv 1,
                dokument faktum "f5" id 5 avhengerAv 1,
                desimaltall faktum "f6" id 6 avhengerAv 1,
                envalg faktum "f7" id 7 med "valg1" med "valg2" avhengerAv 1,
                flervalg faktum "f8" id 8 med "valg1" med "valg2" avhengerAv 1,
                heltall faktum "f9" id 9,
                heltall faktum "f10" id 10,
                heltall faktum "f11" id 11 genererer 9 og 10 avhengerAv 1,
                inntekt faktum "f12" id 12 avhengerAv 1,
                periode faktum "f13" id 13 avhengerAv 1,
                tekst faktum "f14" id 14 avhengerAv 1,
                land faktum "f15" id 15 avhengerAv 1,
            )
            Versjon.Bygger(
                prototypeFakta,
                prototypeFakta boolsk 1 er true,
                mapOf(
                    Web to Faktagrupper(
                        Seksjon(
                            "seksjon",
                            Rolle.nav,
                            *(prototypeFakta.map { it }.toTypedArray())
                        )
                    )
                )
            ).registrer()
            FaktumTable(prototypeFakta)

            søknadRecord = SøknadRecord()
            originalFaktagrupper = søknadRecord.ny(SøknadRecordTest.UNG_PERSON_FNR_2018, Web, prosessVersjon)

            originalFaktagrupper.boolsk(1).besvar(true)
            originalFaktagrupper.dato(2).besvar(10.januar)
            originalFaktagrupper.boolsk(3).besvar(false)
            originalFaktagrupper.heltall(4).besvar(34)
            originalFaktagrupper.dokument(5).besvar(Dokument(LocalDateTime.now(), "urn:si:test:123"))
            originalFaktagrupper.desimaltall(6).besvar(2.4)
            originalFaktagrupper.envalg(7).besvar(Envalg("f7.valg1"))
            originalFaktagrupper.flervalg(8).besvar(Flervalg("f8.valg1", "f8.valg2"))
            originalFaktagrupper.heltall(11).besvar(1)
            originalFaktagrupper.heltall("9.1").besvar(150)
            originalFaktagrupper.heltall("10.1").besvar(160)
            originalFaktagrupper.inntekt(12).besvar(100.årlig)
            originalFaktagrupper.periode(13).besvar(Periode(LocalDate.now().minusYears(2), LocalDate.now()))
            originalFaktagrupper.tekst(14).besvar(Tekst("svar tekst"))
            originalFaktagrupper.land(15).besvar(Land("SWE"))

            assertEquals(15, originalFaktagrupper.søknad.count { it.erBesvart() })
            søknadRecord.lagre(originalFaktagrupper.søknad)

            assertRecordCount(0, "gammel_faktum_verdi")
            rehydrertFaktagrupper = søknadRecord.hent(originalFaktagrupper.søknad.uuid)
            assertEquals(15, rehydrertFaktagrupper.søknad.count { it.erBesvart() })

            originalFaktagrupper.boolsk(1).besvar(false)
            søknadRecord.lagre(originalFaktagrupper.søknad)
            assertRecordCount(15, "gammel_faktum_verdi")
            rehydrertFaktagrupper = søknadRecord.hent(originalFaktagrupper.søknad.uuid)
            assertEquals(1, rehydrertFaktagrupper.søknad.count { it.erBesvart() })
        }
    }

    private fun lagreHentOgSammenlign(userInterfaceType: Versjon.UserInterfaceType = Web) {
        søknadRecord.lagre(originalFaktagrupper.søknad)
        val uuid = SøknadRecord().opprettede(UNG_PERSON_FNR_2018).toSortedMap().values.first()
        rehydrertFaktagrupper = søknadRecord.hent(uuid, userInterfaceType)
        assertDeepEquals(originalFaktagrupper, rehydrertFaktagrupper)
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
