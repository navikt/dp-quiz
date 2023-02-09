package no.nav.dagpenger.quiz.mediator.integration

import no.nav.dagpenger.model.faktum.Dokument
import no.nav.dagpenger.model.faktum.Identer
import no.nav.dagpenger.model.seksjon.Versjon
import no.nav.dagpenger.quiz.mediator.db.FaktaRecord
import no.nav.dagpenger.quiz.mediator.db.FaktumTable
import no.nav.dagpenger.quiz.mediator.helpers.Postgres
import no.nav.dagpenger.quiz.mediator.soknad.Prosessfakta
import no.nav.dagpenger.quiz.mediator.soknad.aldersvurdering.Paragraf_4_23_alder_oppsett
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.UUID
import kotlin.test.assertTrue

class Paragraf_43_alderIntegrasjonsTest {
    @Test
    fun `test at vi kan opprettet Paragraf_4_23_alder i databasen og lagre faktum`() {
        Postgres.withMigratedDb {
            Paragraf_4_23_alder_oppsett.registrer { prototypeSøknad -> FaktumTable(prototypeSøknad) }
            val søknadPersistence = FaktaRecord()
            val identer = Identer.Builder().folkeregisterIdent("12345678910").build()
            val søknadsprosess = søknadPersistence.ny(
                identer,
                Versjon.siste(Prosessfakta.Paragraf_4_23_alder),
            )

            søknadsprosess.dokument(Paragraf_4_23_alder_oppsett.innsendtSøknadId)
                .besvar(Dokument(LocalDateTime.now(), "urn:soknadid:${UUID.randomUUID()}"))

            søknadPersistence.lagre(søknadsprosess.fakta)
            val rehydrertSøknadsprosess =
                søknadPersistence.hent(søknadsprosess.fakta.uuid)

            assertTrue(rehydrertSøknadsprosess.dokument(Paragraf_4_23_alder_oppsett.innsendtSøknadId).erBesvart())
        }
    }
}
