package no.nav.dagpenger.quiz.mediator.db

import PostgresDataSourceBuilder.dataSource
import kotliquery.queryOf
import kotliquery.sessionOf
import kotliquery.using
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.boolsk
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.dato
import no.nav.dagpenger.model.factory.UtledetFaktumFactory.Companion.maks
import no.nav.dagpenger.model.faktum.Identer
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.regel.er
import no.nav.dagpenger.model.seksjon.Seksjon
import no.nav.dagpenger.model.seksjon.Søknadprosess
import no.nav.dagpenger.model.seksjon.Versjon
import no.nav.dagpenger.model.seksjon.Versjon.UserInterfaceType.Web
import no.nav.dagpenger.quiz.mediator.helpers.Postgres
import no.nav.dagpenger.quiz.mediator.helpers.januar
import no.nav.helse.serde.assertDeepEquals
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class AvhengigeFaktaTest {
    companion object {
        internal val UNG_PERSON_FNR_2018 = Identer.Builder().folkeregisterIdent("12020052345").build()
    }

    private lateinit var originalSøknadprosess: Søknadprosess
    private lateinit var rehydrertSøknadprosess: Søknadprosess
    private lateinit var søknadRecord: SøknadRecord

    @Test
    fun `Avhengig faktum reset`() {
        val versjonId = 634
        Postgres.withMigratedDb {
            val prototypeFakta = Søknad(
                versjonId,
                boolsk faktum "f1" id 19 avhengerAv 2 og 13,
                dato faktum "f2" id 2,
                dato faktum "f3" id 13,

                )
            Versjon.Bygger(
                prototypeFakta,
                prototypeFakta boolsk 19 er true,
                mapOf(
                    Web to Søknadprosess(
                        Seksjon(
                            "seksjon",
                            Rolle.nav,
                            *(prototypeFakta.map { it }.toTypedArray())
                        )
                    )
                )
            ).registrer()
            FaktumTable(prototypeFakta, versjonId)
            søknadRecord = SøknadRecord()
            originalSøknadprosess = søknadRecord.ny(SøknadRecordTest.UNG_PERSON_FNR_2018, Web, versjonId)

            originalSøknadprosess.dato(2).besvar(2.januar)
            originalSøknadprosess.dato(13).besvar(13.januar)
            originalSøknadprosess.boolsk(19).besvar(true)
            hentFørsteSøknad()
            assertRecordCount(3, "gammel_faktum_verdi")
            assertTrue(rehydrertSøknadprosess.boolsk(19).svar())
            originalSøknadprosess.dato(2).besvar(22.januar)
            hentFørsteSøknad()
            assertRecordCount(5, "gammel_faktum_verdi")
            assertFalse(rehydrertSøknadprosess.boolsk(19).erBesvart())
        }
    }

    @Test
    fun `Avhengig faktum rehydreres`() {
        val versjonId = 635
        Postgres.withMigratedDb {
            val prototypeFakta = Søknad(
                versjonId,
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
                    Web to Søknadprosess(
                        Seksjon(
                            "seksjon",
                            Rolle.nav,
                            *(prototypeFakta.map { it }.toTypedArray())
                        )
                    )
                )
            ).registrer()
            FaktumTable(prototypeFakta, versjonId)

            søknadRecord = SøknadRecord()
            originalSøknadprosess = søknadRecord.ny(SøknadRecordTest.UNG_PERSON_FNR_2018, Web, versjonId)

            originalSøknadprosess.boolsk(2).besvar(true)
            originalSøknadprosess.boolsk(5).besvar(true)
            originalSøknadprosess.boolsk(4).besvar(true)
            originalSøknadprosess.boolsk(1).besvar(true)
            originalSøknadprosess.boolsk(3).besvar(true)

            assertEquals(5, originalSøknadprosess.søknad.count { it.erBesvart() })
            søknadRecord.lagre(originalSøknadprosess.søknad)

            rehydrertSøknadprosess = søknadRecord.hent(originalSøknadprosess.søknad.uuid)
            assertEquals(5, rehydrertSøknadprosess.søknad.count { it.erBesvart() })
        }
    }

    @Test
    fun `Avhengig av utledet faktum rehydreres`() {
        val versjonId = 636
        Postgres.withMigratedDb {
            val prototypeFakta = Søknad(
                versjonId,
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
                    Web to Søknadprosess(
                        Seksjon(
                            "seksjon",
                            Rolle.nav,
                            *(prototypeFakta.map { it }.toTypedArray())
                        )
                    )
                )
            ).registrer()
            FaktumTable(prototypeFakta, versjonId)

            søknadRecord = SøknadRecord()
            originalSøknadprosess = søknadRecord.ny(SøknadRecordTest.UNG_PERSON_FNR_2018, Web, versjonId)

            originalSøknadprosess.dato(2).besvar(1.januar)
            originalSøknadprosess.dato(3).besvar(10.januar)
            originalSøknadprosess.boolsk(1).besvar(true)
            originalSøknadprosess.boolsk(5).besvar(true)

            assertEquals(5, originalSøknadprosess.søknad.count { it.erBesvart() })
            søknadRecord.lagre(originalSøknadprosess.søknad)

            rehydrertSøknadprosess = søknadRecord.hent(originalSøknadprosess.søknad.uuid)
            assertEquals(5, rehydrertSøknadprosess.søknad.count { it.erBesvart() })
        }
    }

    private fun hentFørsteSøknad(userInterfaceType: Versjon.UserInterfaceType = Web) {
        søknadRecord.lagre(originalSøknadprosess.søknad)
        val uuid = SøknadRecord().opprettede(UNG_PERSON_FNR_2018).toSortedMap().values.first()
        rehydrertSøknadprosess = søknadRecord.hent(uuid, userInterfaceType)
        assertDeepEquals(originalSøknadprosess, rehydrertSøknadprosess)
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
