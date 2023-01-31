package no.nav.dagpenger.quiz.mediator.soknad

import no.nav.dagpenger.model.seksjon.Faktagrupper
import no.nav.dagpenger.model.seksjon.Versjon
import no.nav.dagpenger.quiz.mediator.soknad.avslagminsteinntekt.AvslagPåMinsteinntektOppsett
import no.nav.dagpenger.quiz.mediator.soknad.dagpenger.Dagpenger
import no.nav.dagpenger.quiz.mediator.soknad.innsending.Innsending
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows

internal class ProsessMetadataStrategiTest {

    private lateinit var faktagrupperDagpenger: Faktagrupper
    private lateinit var faktagrupperInnsending: Faktagrupper
    private lateinit var faktagrupperAvslagPåMinsteinntekt: Faktagrupper

    init {
        Dagpenger.registrer { prototypeSøknad ->
            faktagrupperDagpenger = Versjon.id(Dagpenger.VERSJON_ID)
                .søknadprosess(prototypeSøknad, Versjon.UserInterfaceType.Web)
        }

        Innsending.registrer { prototypeSøknad ->
            faktagrupperInnsending = Versjon.id(Innsending.VERSJON_ID)
                .søknadprosess(prototypeSøknad, Versjon.UserInterfaceType.Web)
        }

        AvslagPåMinsteinntektOppsett.registrer { prototype ->
            faktagrupperAvslagPåMinsteinntekt = Versjon.id(AvslagPåMinsteinntektOppsett.VERSJON_ID)
                .søknadprosess(prototype, Versjon.UserInterfaceType.Web)
        }
    }

    @Test
    fun `bestemme hvilken prosess skjemastrategi skal gå etter`() {
        val prosessSkjemakodeStrategi = ProsessMetadataStrategi()
        assertDoesNotThrow { prosessSkjemakodeStrategi.metadata(faktagrupperDagpenger) }
        assertDoesNotThrow { prosessSkjemakodeStrategi.metadata(faktagrupperInnsending) }
        assertThrows<IllegalArgumentException> { prosessSkjemakodeStrategi.metadata(faktagrupperAvslagPåMinsteinntekt) }
    }
}
