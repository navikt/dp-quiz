package no.nav.dagpenger.quiz.mediator.integration

import no.nav.dagpenger.quiz.mediator.db.FaktumTable
import no.nav.dagpenger.quiz.mediator.db.ResultatRecord
import no.nav.dagpenger.quiz.mediator.db.SøknadRecord
import no.nav.dagpenger.quiz.mediator.helpers.Postgres
import no.nav.dagpenger.quiz.mediator.meldinger.DagpengerService
import no.nav.dagpenger.quiz.mediator.meldinger.FaktumSvarService
import no.nav.dagpenger.quiz.mediator.soknad.Dagpenger
import no.nav.dagpenger.quiz.mediator.soknad.Dagpenger.`Avtjent militærtjeneste minst 3 av siste 6 mnd`
import no.nav.dagpenger.quiz.mediator.soknad.Dagpenger.`Villig til å ta alle typer arbeid`
import no.nav.dagpenger.quiz.mediator.soknad.Dagpenger.`Villig til å ta arbeid i hele Norge`
import no.nav.dagpenger.quiz.mediator.soknad.Dagpenger.`Villig til å ta ethvert arbeid`
import no.nav.dagpenger.quiz.mediator.soknad.Dagpenger.`Villig til å ta hel og deltidsjobb`
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DagpengerTest : SøknadBesvarer() {

    @BeforeEach
    fun setup() {
        Postgres.withMigratedDb {
            Dagpenger.registrer { søknad -> FaktumTable(søknad) }
            val søknadPersistence = SøknadRecord()
            val resultatPersistence = ResultatRecord()
            testRapid = TestRapid().also {
                FaktumSvarService(
                    søknadPersistence = søknadPersistence,
                    resultatPersistence = resultatPersistence,
                    rapidsConnection = it
                )
                DagpengerService(søknadPersistence, it)
            }
        }
    }

    @Test
    fun ` Reell arbeidssøker og verneplikt glad sti `() {
        withSøknad(ønskerRettighetsavklaring) { besvar ->
            val firstMessage = testRapid.inspektør.message(0)
            assertEquals(søknadUUID, firstMessage["søknad_uuid"].asText().let { soknadId -> UUID.fromString(soknadId) })
            assertGjeldendeSeksjon("Er reell arbeidssøker")
            besvar(`Villig til å ta hel og deltidsjobb`, true)
            besvar(`Villig til å ta arbeid i hele Norge`, true)
            besvar(`Villig til å ta alle typer arbeid`, true)
            besvar(`Villig til å ta ethvert arbeid`, true)
            assertGjeldendeSeksjon("Har avtjent verneplikt")
            besvar(`Avtjent militærtjeneste minst 3 av siste 6 mnd`, true)
            assertTrue(gjeldendeResultat())
        }
    }

    private val søknadUUID = UUID.randomUUID()

    //language=JSON
    private val ønskerRettighetsavklaring =
        """
        {
          "@event_name": "ønsker_rettighetsavklaring",
          "@opprettet": "${LocalDateTime.now()}",
          "@id": "${UUID.randomUUID()}",
          "søknad_uuid": "$søknadUUID",
          "fødselsnummer": "123456789"
        }
        
        """.trimIndent()
}
