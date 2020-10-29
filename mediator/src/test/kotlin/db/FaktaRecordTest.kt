package db

import DataSourceBuilder.dataSource
import helpers.FaktaEksempel.prototypeFakta
import helpers.Postgres
import kotliquery.queryOf
import kotliquery.sessionOf
import kotliquery.using
import no.nav.dagpenger.model.fakta.Inntekt.Companion.årlig
import no.nav.dagpenger.model.fakta.Rolle
import no.nav.dagpenger.model.søknad.Søknad
import no.nav.dagpenger.model.søknad.Versjon
import no.nav.helse.serde.assertDeepEquals
import org.junit.jupiter.api.Test
import java.time.LocalDate
import kotlin.test.assertEquals

internal class FaktaRecordTest {
    companion object {
        internal const val UNG_PERSON_FNR_2018 = "12020052345"
    }

    private lateinit var originalSøknad: Søknad
    private lateinit var rehydrertSøknad: Søknad
    private lateinit var faktaRecord: FaktaRecord

    @Test
    fun `ny søknad`() {
        Postgres.withMigratedDb {
            byggOriginalSøknad()

            assertRecordCount(1, "fakta")
            assertRecordCount(21, "faktum_verdi")
            FaktaRecord().ny(UNG_PERSON_FNR_2018, Versjon.Type.Web)
            assertRecordCount(2, "fakta")
            assertRecordCount(42, "faktum_verdi")
            hentFørstFakta()
        }
    }

    @Test
    fun `lagring og henting av fakta`() {
        Postgres.withMigratedDb {
            byggOriginalSøknad()

            originalSøknad.ja(1).besvar(true, Rolle.søker)
            originalSøknad.dato(2).besvar(LocalDate.now(), Rolle.søker)
            originalSøknad.inntekt(6).besvar(10000.årlig, Rolle.søker)
            originalSøknad.heltall(16).besvar(123, Rolle.søker)

            hentFørstFakta()
        }
    }

    @Test
    fun `dependent faktum reset`() {
    }

    @Test
    fun `Genererte template faktum`() {
        Postgres.withMigratedDb {
            byggOriginalSøknad()
            assertEquals(21, originalSøknad.fakta.map { it }.size)
            hentFørstFakta()
            originalSøknad = rehydrertSøknad

            originalSøknad.heltall(15).besvar(3, Rolle.søker)
            originalSøknad.heltall("16.1").besvar(5, Rolle.søker)
            assertEquals(30, originalSøknad.fakta.map { it }.size)

            hentFørstFakta()
            assertEquals(30, rehydrertSøknad.fakta.map { it }.size)
        }
    }

    @Test
    fun `reduced template faktum`() {
    }

    @Test
    fun `utledet faktum with value`() {
    }

    private fun hentFørstFakta() {
        faktaRecord.lagre(originalSøknad.fakta)
        val uuid = FaktaRecord().opprettede(UNG_PERSON_FNR_2018).toSortedMap().values.first()
        faktaRecord = FaktaRecord()
        rehydrertSøknad = faktaRecord.hent(uuid, Versjon.Type.Web)
        assertDeepEquals(originalSøknad, rehydrertSøknad)
    }

    private fun byggOriginalSøknad() {
        FaktumTable(prototypeFakta, 1)
        faktaRecord = FaktaRecord()
        originalSøknad = faktaRecord.ny(UNG_PERSON_FNR_2018, Versjon.Type.Web)
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
