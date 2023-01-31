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
            FaktumTable(SøknadEksempel1.prototypeFakta1)
            FaktumTable(SøknadEksempel.prototypeFakta1)

            val faktaRecord = FaktaRecord()

            val søknad1 = faktaRecord.ny(
                Identer.Builder().folkeregisterIdent("10987654321").build(),
                SøknadEksempel1.prosessVersjon
            )
            val søknad2 = faktaRecord.ny(
                Identer.Builder().folkeregisterIdent("12345678910").build(),
                SøknadEksempel.prosessVersjon
            )

            søknad1.boolsk(10).besvar(true)
            faktaRecord.lagre(søknad1.fakta)

            søknad2.boolsk(8).besvar(false)
            faktaRecord.lagre(søknad2.fakta)

            val rehydrertSøknadprosess1 = faktaRecord.hent(søknad1.fakta.uuid)
            val rehydrertSøknadprosess2 = faktaRecord.hent(søknad2.fakta.uuid)

            assertEquals(true, rehydrertSøknadprosess1.fakta.boolsk(10).svar())
            assertEquals(false, rehydrertSøknadprosess2.fakta.boolsk(8).svar())
        }
    }
}
