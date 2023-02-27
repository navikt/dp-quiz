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
import no.nav.dagpenger.model.faktum.Fakta
import no.nav.dagpenger.model.faktum.Faktaversjon
import no.nav.dagpenger.model.faktum.Flervalg
import no.nav.dagpenger.model.faktum.Identer
import no.nav.dagpenger.model.faktum.Inntekt.Companion.årlig
import no.nav.dagpenger.model.faktum.Land
import no.nav.dagpenger.model.faktum.Periode
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.faktum.Tekst
import no.nav.dagpenger.model.helpers.assertDeepEquals
import no.nav.dagpenger.model.helpers.januar
import no.nav.dagpenger.model.regel.er
import no.nav.dagpenger.model.seksjon.Prosess
import no.nav.dagpenger.model.seksjon.Prosesstype
import no.nav.dagpenger.model.seksjon.Prosessversjon
import no.nav.dagpenger.model.seksjon.Seksjon
import no.nav.dagpenger.quiz.mediator.db.PostgresDataSourceBuilder.dataSource
import no.nav.dagpenger.quiz.mediator.helpers.Postgres
import no.nav.dagpenger.quiz.mediator.helpers.Testfakta
import no.nav.dagpenger.quiz.mediator.helpers.Testprosess
import no.nav.dagpenger.quiz.mediator.helpers.registrer
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

    private lateinit var originalProsess: Prosess
    private lateinit var rehydrertProsess: Prosess
    private lateinit var prosessRepository: ProsessRepository

    @Test
    fun `Avhengig faktum reset`() {
        val prosessVersjon = Faktaversjon(Testfakta.Test, 634)
        Postgres.withMigratedDb {
            val prototypeFakta = Fakta(
                prosessVersjon,
                boolsk faktum "f1" id 19 avhengerAv 2 og 13,
                dato faktum "f2" id 2,
                dato faktum "f3" id 13,
            ).registrer()
            Prosessversjon.Bygger(
                Testfakta.Test,
                prototypeFakta boolsk 19 er true,
                Prosess(
                    Testprosess.Test,
                    Seksjon(
                        "seksjon",
                        Rolle.nav,
                        *(prototypeFakta.map { it }.toTypedArray()),
                    ),
                ),
            ).registrer()
            FaktumTable(prototypeFakta)
            prosessRepository = ProsessRepositoryImpl()
            originalProsess = utredningsprosess(Testprosess.Test)

            originalProsess.dato(2).besvar(2.januar)
            originalProsess.dato(13).besvar(13.januar)
            originalProsess.boolsk(19).besvar(true)
            lagreHentOgSammenlign()
            assertRecordCount(0, "gammel_faktum_verdi")
            assertTrue(rehydrertProsess.boolsk(19).svar())
            originalProsess.dato(2).besvar(22.januar)
            lagreHentOgSammenlign()
            assertRecordCount(2, "gammel_faktum_verdi")
            assertFalse(rehydrertProsess.boolsk(19).erBesvart())
        }
    }

    @Test
    fun `Avhengig faktum rehydreres`() {
        val prosessVersjon = Faktaversjon(Testfakta.Test, 635)

        Postgres.withMigratedDb {
            val prototypeFakta = Fakta(
                prosessVersjon,
                boolsk faktum "f1" id 1 avhengerAv 4,
                boolsk faktum "f2" id 2,
                boolsk faktum "f3" id 3 avhengerAv 1,
                boolsk faktum "f4" id 4 avhengerAv 5,
                boolsk faktum "f5" id 5,
            ).registrer()
            Prosessversjon.Bygger(
                Testfakta.Test,
                prototypeFakta boolsk 1 er true,
                Prosess(
                    Testprosess.Test,
                    Seksjon(
                        "seksjon",
                        Rolle.nav,
                        *(prototypeFakta.map { it }.toTypedArray()),
                    ),
                ),
            ).registrer()
            FaktumTable(prototypeFakta)

            prosessRepository = ProsessRepositoryImpl()
            originalProsess = utredningsprosess(Testprosess.Test)

            originalProsess.boolsk(2).besvar(true)
            originalProsess.boolsk(5).besvar(true)
            originalProsess.boolsk(4).besvar(true)
            originalProsess.boolsk(1).besvar(true)
            originalProsess.boolsk(3).besvar(true)

            assertEquals(5, originalProsess.fakta.count { it.erBesvart() })
            prosessRepository.lagre(originalProsess)

            rehydrertProsess = prosessRepository.hent(originalProsess.fakta.uuid)
            assertEquals(5, rehydrertProsess.fakta.count { it.erBesvart() })
        }
    }

    @Test
    fun `Avhengig av utledet faktum rehydreres`() {
        val prosessVersjon = Faktaversjon(Testfakta.Test, 636)

        Postgres.withMigratedDb {
            val prototypeFakta = Fakta(
                prosessVersjon,
                boolsk faktum "f1" id 1 avhengerAv 4,
                dato faktum "f2" id 2,
                dato faktum "f3" id 3,
                maks dato "f4" av 2 og 3 id 4,
                boolsk faktum "f1" id 5 avhengerAv 4,
            ).registrer()
            Prosessversjon.Bygger(
                Testfakta.Test,
                prototypeFakta boolsk 1 er true,
                Prosess(
                    Testprosess.Test,
                    Seksjon(
                        "seksjon",
                        Rolle.nav,
                        *(prototypeFakta.map { it }.toTypedArray()),
                    ),
                ),
            ).registrer()
            FaktumTable(prototypeFakta)

            prosessRepository = ProsessRepositoryImpl()
            originalProsess = utredningsprosess(Testprosess.Test)

            originalProsess.dato(2).besvar(1.januar)
            originalProsess.dato(3).besvar(10.januar)
            originalProsess.boolsk(1).besvar(true)
            originalProsess.boolsk(5).besvar(true)

            assertEquals(5, originalProsess.fakta.count { it.erBesvart() })
            prosessRepository.lagre(originalProsess)

            rehydrertProsess = prosessRepository.hent(originalProsess.fakta.uuid)
            assertEquals(5, rehydrertProsess.fakta.count { it.erBesvart() })
        }
    }

    @Test
    fun `Alle avhengige faktumtyper resettes`() {
        val prosessVersjon = Faktaversjon(Testfakta.Test, 637)
        Postgres.withMigratedDb {
            val prototypeFakta = Fakta(
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
            ).registrer()
            Prosessversjon.Bygger(
                Testfakta.Test,
                prototypeFakta boolsk 1 er true,
                Prosess(
                    Testprosess.Test,
                    Seksjon(
                        "seksjon",
                        Rolle.nav,
                        *(prototypeFakta.map { it }.toTypedArray()),
                    ),
                ),
            ).registrer()
            FaktumTable(prototypeFakta)

            prosessRepository = ProsessRepositoryImpl()
            originalProsess = utredningsprosess(Testprosess.Test)

            originalProsess.boolsk(1).besvar(true)
            originalProsess.dato(2).besvar(10.januar)
            originalProsess.boolsk(3).besvar(false)
            originalProsess.heltall(4).besvar(34)
            originalProsess.dokument(5).besvar(Dokument(LocalDateTime.now(), "urn:si:test:123"))
            originalProsess.desimaltall(6).besvar(2.4)
            originalProsess.envalg(7).besvar(Envalg("f7.valg1"))
            originalProsess.flervalg(8).besvar(Flervalg("f8.valg1", "f8.valg2"))
            originalProsess.heltall(11).besvar(1)
            originalProsess.heltall("9.1").besvar(150)
            originalProsess.heltall("10.1").besvar(160)
            originalProsess.inntekt(12).besvar(100.årlig)
            originalProsess.periode(13).besvar(Periode(LocalDate.now().minusYears(2), LocalDate.now()))
            originalProsess.tekst(14).besvar(Tekst("svar tekst"))
            originalProsess.land(15).besvar(Land("SWE"))

            assertEquals(15, originalProsess.fakta.count { it.erBesvart() })
            prosessRepository.lagre(originalProsess)

            assertRecordCount(0, "gammel_faktum_verdi")
            rehydrertProsess = prosessRepository.hent(originalProsess.uuid)
            assertEquals(rehydrertProsess.tekst("14").svar(), Tekst("svar tekst"))
            assertEquals(15, rehydrertProsess.fakta.count { it.erBesvart() })

            originalProsess.boolsk(1).besvar(false)
            prosessRepository.lagre(originalProsess)
            assertRecordCount(15, "gammel_faktum_verdi")
            rehydrertProsess = prosessRepository.hent(originalProsess.uuid)
            assertEquals(1, rehydrertProsess.fakta.count { it.erBesvart() })
        }
    }

    private fun utredningsprosess(prosesstype: Prosesstype): Prosess {
        return prosessRepository.ny(UNG_PERSON_FNR_2018, prosesstype)
    }

    private fun lagreHentOgSammenlign() {
        prosessRepository.lagre(originalProsess)
        val uuid = FaktaRecord().opprettede(UNG_PERSON_FNR_2018).toSortedMap().values.first()
        rehydrertProsess = prosessRepository.hent(uuid)
        assertDeepEquals(originalProsess, rehydrertProsess)
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
        )
    }
}
