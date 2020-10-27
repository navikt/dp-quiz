package db

import helpers.FaktaEksempel.prototypeFakta
import helpers.Postgres
import no.nav.dagpenger.model.søknad.Versjon
import org.junit.jupiter.api.Test

internal class FaktaRecordTest {
    companion object {
        internal const val UNG_PERSON_FNR_2018 = "12020052345"
    }

    @Test
    fun `ny søknad`() {
        Postgres.withMigratedDb {
            FaktumTable(prototypeFakta, 1)
            val ny = FaktaRecord().ny(UNG_PERSON_FNR_2018, Versjon.Type.Web)
        }
    }
}
