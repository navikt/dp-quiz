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

class GjenopptakTest {
    private lateinit var gjenopptak: Søknadprosess

    @BeforeEach
    fun setup() {
        gjenopptak = Versjon.Bygger(
            Gjenopptak.søknad,
            SkalBeslutteGjenopptak.sjekkGjenopptak,
            mapOf(Versjon.UserInterfaceType.Web to GjenopptakSeksjoner.gjenopptakSøknadsprosess)
        )
            .søknadprosess(
                Person(UUID.randomUUID(), Identer.Builder().folkeregisterIdent("12345678910").build()),
                Versjon.UserInterfaceType.Web
            )

        gjenopptak.apply {
            this.boolsk(Gjenopptak.gjenopptak).besvar(true)
        }
    }

    @Test
    fun `Besvarte gjenopptak med Ja`() {
        assertTrue(gjenopptak.erFerdig())
        assertEquals(true, gjenopptak.resultat())
    }
}
