package no.nav.dagpenger.quiz.mediator.db

import no.nav.dagpenger.model.faktum.Identer
import no.nav.dagpenger.quiz.mediator.helpers.Postgres
import no.nav.dagpenger.quiz.mediator.helpers.SøknadEksempel
import no.nav.dagpenger.quiz.mediator.helpers.SøknadEksempel1
import no.nav.dagpenger.quiz.mediator.helpers.Testprosess
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class FlereSøknaderTest {
    @Test
    fun `takler flere søknader samtidig`() {
        Postgres.withMigratedDb {
            FaktumTable(SøknadEksempel1.prototypeFakta1)
            FaktumTable(SøknadEksempel.prototypeFakta1)
            val faktaRecord = FaktaRecord()
            val søknad1 = faktaRecord.ny(
                Identer.Builder().folkeregisterIdent("10987654321").build(),
                Testprosess.Test,
            )
            val søknad2 = faktaRecord.ny(
                Identer.Builder().folkeregisterIdent("12345678910").build(),
                Testprosess.Test,
            )

            søknad1.boolsk(10).besvar(true)
            faktaRecord.lagre(søknad1)

            søknad2.boolsk(8).besvar(false)
            faktaRecord.lagre(søknad2)
            val rehydrertSøknadprosess1 = faktaRecord.hent(søknad1.uuid)
            val rehydrertSøknadprosess2 = faktaRecord.hent(søknad2.uuid)

            assertEquals(true, rehydrertSøknadprosess1.boolsk(10).svar())
            assertEquals(false, rehydrertSøknadprosess2.boolsk(8).svar())
        }
    }
}
