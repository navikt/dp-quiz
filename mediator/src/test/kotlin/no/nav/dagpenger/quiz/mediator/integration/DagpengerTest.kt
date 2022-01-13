package no.nav.dagpenger.quiz.mediator.integration

import no.nav.dagpenger.quiz.mediator.db.FaktumTable
import no.nav.dagpenger.quiz.mediator.db.ResultatRecord
import no.nav.dagpenger.quiz.mediator.db.SøknadRecord
import no.nav.dagpenger.quiz.mediator.helpers.Postgres
import no.nav.dagpenger.quiz.mediator.meldinger.DagpengerService
import no.nav.dagpenger.quiz.mediator.meldinger.FaktumSvarService
import no.nav.dagpenger.quiz.mediator.soknad.Dagpenger
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.UUID
import kotlin.test.assertEquals

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
        withSøknad(nySøknad) { besvar ->
            val firstMessage = testRapid.inspektør.message(0)
            assertEquals(søknadUUID, firstMessage["søknad_uuid"].asText().let { soknadId -> UUID.fromString(soknadId) })
            assertEquals(8, firstMessage["fakta"].size())
        }
    }

    private val søknadUUID = UUID.randomUUID()

    //language=JSON
    private val nySøknad =
        """
        {
          "@event_name": "NySøknad",
          "@opprettet": "${LocalDateTime.now()}",
          "@id": "${UUID.randomUUID()}",
          "søknad_uuid": "$søknadUUID",
          "fødselsnummer": "123456789"
        }
        
        """.trimIndent()
}
