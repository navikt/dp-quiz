package no.nav.dagpenger.quiz.mediator.soknad

import no.nav.dagpenger.model.faktum.Identer
import no.nav.dagpenger.model.faktum.Person
import no.nav.dagpenger.model.seksjon.Søknadprosess
import no.nav.dagpenger.model.seksjon.Versjon
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.UUID

class DagpengerTest {
    private lateinit var dagpenger: Søknadprosess

    @BeforeEach
    fun setup() {
        dagpenger = Versjon.Bygger(
            Dagpenger.søknad,
            Dagpenger.regeltre,
            mapOf(Versjon.UserInterfaceType.Web to Dagpenger.søknadsprosess)
        )
            .søknadprosess(
                Person(UUID.randomUUID(), Identer.Builder().folkeregisterIdent("12345678910").build()),
                Versjon.UserInterfaceType.Web
            )

        dagpenger.apply {
            this.boolsk(Dagpenger.`Har du hatt dagpenger i løpet av de siste 52 ukene`).besvar(true)
        }
    }

    @Test
    fun `Besvarte gjenopptak med Ja`() {
        assertTrue(dagpenger.erFerdig())
        assertEquals(true, dagpenger.resultat())
    }
}
