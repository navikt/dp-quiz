package no.nav.dagpenger.quiz.mediator.integration

import no.nav.dagpenger.quiz.mediator.db.FaktumTable
import no.nav.dagpenger.quiz.mediator.db.ResultatRecord
import no.nav.dagpenger.quiz.mediator.db.SøknadRecord
import no.nav.dagpenger.quiz.mediator.helpers.Postgres
import no.nav.dagpenger.quiz.mediator.meldinger.FaktumSvarService
import no.nav.dagpenger.quiz.mediator.meldinger.GjenopptakService
import no.nav.dagpenger.quiz.mediator.soknad.Gjenopptak
import no.nav.dagpenger.quiz.mediator.soknad.Gjenopptak.`Har du hatt dagpenger i løpet av de siste 52 ukene`
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GjenopptakTest : SøknadBesvarer() {

    @BeforeEach
    fun setup() {
        Postgres.withMigratedDb {
            Gjenopptak.registrer { søknad, versjonId -> FaktumTable(søknad, versjonId) }
            val søknadPersistence = SøknadRecord()
            val resultatPersistence = ResultatRecord()
            testRapid = TestRapid().also {
                FaktumSvarService(
                    søknadPersistence = søknadPersistence,
                    resultatPersistence = resultatPersistence,
                    rapidsConnection = it
                )
                GjenopptakService(søknadPersistence, it, Gjenopptak.VERSJON_ID)
            }
        }
    }

    @Test
    fun `Svar på om bruker har noe å gjenoppta`() {
        withSøknad(ønskerRettighetsavklaring) { besvar ->
            val firstMessage = testRapid.inspektør.message(0)
            assertEquals(søknadUUID, firstMessage["søknad_uuid"].asText().let { soknadId -> UUID.fromString(soknadId) })
            assertGjeldendeSeksjon("gjenopptak")
            besvar(`Har du hatt dagpenger i løpet av de siste 52 ukene`, true)
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
