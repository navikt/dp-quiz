package no.nav.dagpenger.quiz.mediator.soknad

import no.nav.dagpenger.model.seksjon.Henvendelser
import no.nav.dagpenger.model.seksjon.Prosess
import no.nav.dagpenger.quiz.mediator.helpers.testPerson
import no.nav.dagpenger.quiz.mediator.soknad.dagpenger.Dagpenger
import no.nav.dagpenger.quiz.mediator.soknad.innsending.Innsending
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow

internal class ProsessfaktaMetadataStrategiTest {
    private lateinit var prosessDagpenger: Prosess
    private lateinit var prosessInnsending: Prosess

    init {
        Dagpenger.registrer()
        prosessDagpenger = Henvendelser.prosess(testPerson, Prosesser.Søknad)

        Innsending.registrer()
        prosessInnsending = Henvendelser.prosess(testPerson, Prosesser.Innsending)
    }

    @Test
    fun `bestemme hvilken prosess skjemastrategi skal gå etter`() {
        val prosessSkjemakodeStrategi = ProsessMetadataStrategi()
        assertDoesNotThrow { prosessSkjemakodeStrategi.metadata(prosessDagpenger) }
        assertDoesNotThrow { prosessSkjemakodeStrategi.metadata(prosessInnsending) }
    }
}
