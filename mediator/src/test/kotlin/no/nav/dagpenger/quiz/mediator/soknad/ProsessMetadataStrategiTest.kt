package no.nav.dagpenger.quiz.mediator.soknad

import no.nav.dagpenger.model.seksjon.Utredningsprosess
import no.nav.dagpenger.model.seksjon.Versjon
import no.nav.dagpenger.quiz.mediator.soknad.avslagminsteinntekt.AvslagPåMinsteinntektOppsett
import no.nav.dagpenger.quiz.mediator.soknad.dagpenger.Dagpenger
import no.nav.dagpenger.quiz.mediator.soknad.innsending.Innsending
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows

internal class ProsessMetadataStrategiTest {

    private lateinit var utredningsprosessDagpenger: Utredningsprosess
    private lateinit var utredningsprosessInnsending: Utredningsprosess
    private lateinit var utredningsprosessAvslagPåMinsteinntekt: Utredningsprosess

    init {
        Dagpenger.registrer { prototypeSøknad ->
            utredningsprosessDagpenger = Versjon.id(Dagpenger.VERSJON_ID)
                .utredningsprosess(prototypeSøknad)
        }

        Innsending.registrer { prototypeSøknad ->
            utredningsprosessInnsending = Versjon.id(Innsending.VERSJON_ID)
                .utredningsprosess(prototypeSøknad)
        }

        AvslagPåMinsteinntektOppsett.registrer { prototype ->
            utredningsprosessAvslagPåMinsteinntekt = Versjon.id(AvslagPåMinsteinntektOppsett.VERSJON_ID)
                .utredningsprosess(prototype)
        }
    }

    @Test
    fun `bestemme hvilken prosess skjemastrategi skal gå etter`() {
        val prosessSkjemakodeStrategi = ProsessMetadataStrategi()
        assertDoesNotThrow { prosessSkjemakodeStrategi.metadata(utredningsprosessDagpenger) }
        assertDoesNotThrow { prosessSkjemakodeStrategi.metadata(utredningsprosessInnsending) }
        assertThrows<IllegalArgumentException> { prosessSkjemakodeStrategi.metadata(utredningsprosessAvslagPåMinsteinntekt) }
    }
}
