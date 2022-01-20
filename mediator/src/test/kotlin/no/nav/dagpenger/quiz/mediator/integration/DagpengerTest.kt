package no.nav.dagpenger.quiz.mediator.integration

import no.nav.dagpenger.quiz.mediator.db.FaktumTable
import no.nav.dagpenger.quiz.mediator.db.ResultatRecord
import no.nav.dagpenger.quiz.mediator.db.SøknadRecord
import no.nav.dagpenger.quiz.mediator.helpers.Postgres
import no.nav.dagpenger.quiz.mediator.meldinger.DagpengerService
import no.nav.dagpenger.quiz.mediator.meldinger.FaktumSvarService
import no.nav.dagpenger.quiz.mediator.soknad.Dagpenger
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID
import kotlin.test.assertEquals

internal class DagpengerTest : SøknadBesvarer() {

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

    @AfterEach
    fun reset() {
        testRapid.reset()
    }

    @Test
    fun `Hent alle fakta happy path`() {

        withSøknad(nySøknad) { besvar ->
            besvar(
                Dagpenger.arbeidsforhold,
                listOf(
                    listOf(
                        "${Dagpenger.`arbeidsforhold fra og med`}.1" to LocalDate.now().minusYears(5),
                        "${Dagpenger.`arbeidsforhold til og med`}.1" to LocalDate.now().minusMonths(1),
                    )
                )
            )
        }

        assertEquals(3, testRapid.inspektør.size)

        val behov = testRapid.inspektør.message(0)
        assertEquals(listOf("Arbeidsforhold"), behov["@behov"].map { it.asText() })
        val fakta = testRapid.inspektør.message(1)
        assertEquals(søknadUUID, fakta["søknad_uuid"].asText().let { soknadId -> UUID.fromString(soknadId) })
        assertEquals(9, fakta["fakta"].size())

        val besvartFakta = testRapid.inspektør.message(2)
        assertEquals(9, besvartFakta["fakta"].size())
        assertEquals(søknadUUID, besvartFakta["søknad_uuid"].asText().let { soknadId -> UUID.fromString(soknadId) })
    }

    @Test
    fun `Ignore nysøknad med fakta`() {
        testRapid.sendTestMessage(ferdigSøknad)
        assertEquals(0, testRapid.inspektør.size)
    }

    private val søknadUUID = UUID.randomUUID()

    //language=JSON
    private val ferdigSøknad =
        """
        {
          "@event_name": "NySøknad",
          "@opprettet": "${LocalDateTime.now()}",
          "@id": "${UUID.randomUUID()}",
          "søknad_uuid": "${UUID.randomUUID()}",
          "fødselsnummer": "123456789",
          "fakta": []
        }
        
        """.trimIndent()

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
