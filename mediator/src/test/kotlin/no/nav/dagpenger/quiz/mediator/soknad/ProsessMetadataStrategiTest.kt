package no.nav.dagpenger.quiz.mediator.soknad

import no.nav.dagpenger.model.seksjon.Søknadprosess
import no.nav.dagpenger.model.seksjon.Versjon
import no.nav.dagpenger.quiz.mediator.soknad.avslagminsteinntekt.AvslagPåMinsteinntektOppsett
import no.nav.dagpenger.quiz.mediator.soknad.dagpenger.Dagpenger
import no.nav.dagpenger.quiz.mediator.soknad.innsending.Innsending
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows

internal class ProsessMetadataStrategiTest {

    private lateinit var søknadprosessDagpenger: Søknadprosess
    private lateinit var søknadprosessInnsending: Søknadprosess
    private lateinit var søknadprosessAvslagPåMinsteinntekt: Søknadprosess

    init {
        Dagpenger.registrer { prototypeSøknad ->
            søknadprosessDagpenger = Versjon.id(Dagpenger.VERSJON_ID)
                .søknadprosess(prototypeSøknad, Versjon.UserInterfaceType.Web)
        }

        Innsending.registrer { prototypeSøknad ->
            søknadprosessInnsending = Versjon.id(Innsending.VERSJON_ID)
                .søknadprosess(prototypeSøknad, Versjon.UserInterfaceType.Web)
        }

        AvslagPåMinsteinntektOppsett.registrer { prototype ->
            søknadprosessAvslagPåMinsteinntekt = Versjon.id(AvslagPåMinsteinntektOppsett.VERSJON_ID)
                .søknadprosess(prototype, Versjon.UserInterfaceType.Web)
        }
    }

    @Test
    fun `bestemme hvilken prosess skjemastrategi skal gå etter`() {
        val prosessSkjemakodeStrategi = ProsessMetadataStrategi()
        assertDoesNotThrow { prosessSkjemakodeStrategi.metadata(søknadprosessDagpenger) }
        assertDoesNotThrow { prosessSkjemakodeStrategi.metadata(søknadprosessInnsending) }
        assertThrows<IllegalArgumentException> { prosessSkjemakodeStrategi.metadata(søknadprosessAvslagPåMinsteinntekt) }
    }
}
