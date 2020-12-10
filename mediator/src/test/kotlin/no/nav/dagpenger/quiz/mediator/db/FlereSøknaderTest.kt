package no.nav.dagpenger.quiz.mediator.db

import no.nav.dagpenger.model.faktum.Identer
import no.nav.dagpenger.model.seksjon.Versjon
import no.nav.dagpenger.quiz.mediator.helpers.Postgres
import no.nav.dagpenger.quiz.mediator.helpers.SøknadEksempel.Companion.søknadEksempel
import no.nav.dagpenger.quiz.mediator.helpers.SøknadEksempel1.Companion.søknadEksempel1
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class FlereSøknaderTest {
    @Test
    fun `takler flere søknader samtidig`() {
        Postgres.withMigratedDb {
            FaktumTable(søknadEksempel1.prototypeFakta1, 15)
            FaktumTable(søknadEksempel.prototypeSøknad1, 666)

            val søknadRecord = SøknadRecord()

            val søknad1 = søknadRecord.ny(
                Identer.Builder().folkeregisterIdent("10987654321").build(),
                Versjon.UserInterfaceType.Web,
                15
            )
            val søknad2 = søknadRecord.ny(
                Identer.Builder().folkeregisterIdent("12345678910").build(),
                Versjon.UserInterfaceType.Web,
                666
            )

            søknad1.ja(10).besvar(true)
            søknadRecord.lagre(søknad1.søknad)

            søknad2.ja(8).besvar(false)
            søknadRecord.lagre(søknad2.søknad)

            val rehydrertSøknadprosess1 = søknadRecord.hent(søknad1.søknad.uuid)
            val rehydrertSøknadprosess2 = søknadRecord.hent(søknad2.søknad.uuid)

            assertEquals(true, rehydrertSøknadprosess1.søknad.ja(10).svar())
            assertEquals(false, rehydrertSøknadprosess2.søknad.ja(8).svar())
        }
    }
}
