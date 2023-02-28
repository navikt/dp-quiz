package no.nav.dagpenger.quiz.mediator.soknad

import no.nav.dagpenger.model.seksjon.FaktaVersjonDingseboms
import no.nav.dagpenger.model.seksjon.Prosess
import no.nav.dagpenger.quiz.mediator.helpers.testPerson
import no.nav.dagpenger.quiz.mediator.soknad.avslagminsteinntekt.AvslagPåMinsteinntektOppsett
import no.nav.dagpenger.quiz.mediator.soknad.dagpenger.Dagpenger
import no.nav.dagpenger.quiz.mediator.soknad.innsending.Innsending
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows

internal class ProsessfaktaMetadataStrategiTest {
    private lateinit var prosessDagpenger: Prosess
    private lateinit var prosessInnsending: Prosess
    private lateinit var prosessAvslagPåMinsteinntekt: Prosess

    init {
        Dagpenger.registrer()
        prosessDagpenger = FaktaVersjonDingseboms.prosess(testPerson, Prosesser.Søknad)

        Innsending.registrer()
        prosessInnsending = FaktaVersjonDingseboms.prosess(testPerson, Prosesser.Innsending)

        AvslagPåMinsteinntektOppsett.registrer()
        prosessAvslagPåMinsteinntekt = FaktaVersjonDingseboms.prosess(testPerson, Prosesser.AvslagPåMinsteinntekt)
    }

    @Test
    fun `bestemme hvilken prosess skjemastrategi skal gå etter`() {
        val prosessSkjemakodeStrategi = ProsessMetadataStrategi()
        assertDoesNotThrow { prosessSkjemakodeStrategi.metadata(prosessDagpenger) }
        assertDoesNotThrow { prosessSkjemakodeStrategi.metadata(prosessInnsending) }
        assertThrows<IllegalArgumentException> { prosessSkjemakodeStrategi.metadata(prosessAvslagPåMinsteinntekt) }
    }
}
