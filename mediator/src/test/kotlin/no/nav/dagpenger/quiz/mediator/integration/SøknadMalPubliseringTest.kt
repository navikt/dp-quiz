package no.nav.dagpenger.quiz.mediator.integration

import no.nav.dagpenger.model.marshalling.SøknadsmalJsonBuilder
import no.nav.dagpenger.model.seksjon.Prosess
import no.nav.dagpenger.quiz.mediator.db.FaktumTable
import no.nav.dagpenger.quiz.mediator.helpers.Postgres.withMigratedDb
import no.nav.dagpenger.quiz.mediator.soknad.dagpenger.Dagpenger
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertFalse
import kotlin.test.assertEquals

class SøknadMalPubliseringTest {
    @Test
    fun `publiserer mal for en prosess`() {
        withMigratedDb {
            Dagpenger.registrer { prototype: Prosess ->
                FaktumTable(prototype.fakta)
                val malJson = SøknadsmalJsonBuilder(prototype).resultat()

                assertEquals("Søknadsmal", malJson["@event_name"].asText())
                assertFalse(malJson.has("søknad_uuid"))
                assertFalse(malJson.has("fødselsnummer"))
            }
        }
    }
}
