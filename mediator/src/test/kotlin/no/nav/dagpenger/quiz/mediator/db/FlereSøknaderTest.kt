package no.nav.dagpenger.quiz.mediator.db

import no.nav.dagpenger.model.faktum.Identer
import no.nav.dagpenger.quiz.mediator.helpers.Postgres
import no.nav.dagpenger.quiz.mediator.helpers.SøknadEksempel
import no.nav.dagpenger.quiz.mediator.helpers.SøknadEksempel1
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class FlereSøknaderTest {
    @Test
    fun `takler flere søknader samtidig`() {
        Postgres.withMigratedDb {
            FaktumTable(SøknadEksempel1.prototypeFakta)
            FaktumTable(SøknadEksempel.prototypeFakta)
            val faktaRecord = FaktaRecord()
            val fakta1 = faktaRecord.ny(
                Identer.Builder().folkeregisterIdent("10987654321").build(),
                SøknadEksempel1.prosesstype,
            )
            val fakta2 = faktaRecord.ny(
                Identer.Builder().folkeregisterIdent("12345678910").build(),
                SøknadEksempel.prosesstype,
            )

            fakta1.boolsk(10).besvar(true)
            faktaRecord.lagre(fakta1)

            fakta2.boolsk(8).besvar(false)
            faktaRecord.lagre(fakta2)
            val rehydrertSøknadprosess1 = faktaRecord.hent(fakta1.uuid)
            val rehydrertSøknadprosess2 = faktaRecord.hent(fakta2.uuid)

            assertEquals(true, rehydrertSøknadprosess1.boolsk(10).svar())
            assertEquals(false, rehydrertSøknadprosess2.boolsk(8).svar())
        }
    }
}
