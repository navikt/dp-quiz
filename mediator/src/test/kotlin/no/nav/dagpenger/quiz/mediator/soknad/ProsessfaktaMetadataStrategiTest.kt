package no.nav.dagpenger.quiz.mediator.soknad

import no.nav.dagpenger.model.seksjon.Prosess
import no.nav.dagpenger.model.seksjon.Versjon
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
        Dagpenger.registrer { prototypeSøknad ->
            prosessDagpenger = Versjon.id(Prosesser.Søknad)
                .utredningsprosess(prototypeSøknad)
        }

        Innsending.registrer { prototypeSøknad ->
            prosessInnsending = Versjon.id(Prosesser.Innsending)
                .utredningsprosess(prototypeSøknad)
        }

        AvslagPåMinsteinntektOppsett.registrer { prototype ->
            prosessAvslagPåMinsteinntekt = Versjon.id(Prosesser.AvslagPåMinsteinntekt)
                .utredningsprosess(prototype)
        }
    }

    @Test
    fun `bestemme hvilken prosess skjemastrategi skal gå etter`() {
        val prosessSkjemakodeStrategi = ProsessMetadataStrategi()
        assertDoesNotThrow { prosessSkjemakodeStrategi.metadata(prosessDagpenger) }
        assertDoesNotThrow { prosessSkjemakodeStrategi.metadata(prosessInnsending) }
        assertThrows<IllegalArgumentException> { prosessSkjemakodeStrategi.metadata(prosessAvslagPåMinsteinntekt) }
    }
}
