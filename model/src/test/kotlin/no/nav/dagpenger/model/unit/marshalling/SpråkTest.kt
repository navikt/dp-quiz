package no.nav.dagpenger.model.unit.marshalling

import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.dato
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.heltall
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.helpers.januar
import no.nav.dagpenger.model.marshalling.Språk
import no.nav.dagpenger.model.marshalling.Språk.Companion.nynorsk
import no.nav.dagpenger.model.seksjon.Seksjon
import no.nav.dagpenger.model.seksjon.Søknadprosess
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.Locale

internal class SpråkTest {

    private companion object {
        private val versjonId = 1

        private val søknad = Søknad(
            versjonId,
            dato faktum "dato navn" id 1,
            heltall faktum "heltall navn" id 2 genererer 3 og 4,
            dato faktum "dato generert" id 3,
            heltall faktum "heltall generert" id 4,
        )

        private val søknadprosess = Søknadprosess(
            søknad,
            Seksjon(
                "test",
                Rolle.nav,
                søknad dato 1,
                søknad generator 2,
                søknad dato 3,
                søknad heltall 4,
            )
        )
    }

    @Test
    fun `skal oversette fakta fra bokmål til nynorsk `() {
        with(Språk(lokal = nynorsk, versjonId = versjonId)) {
            assertEquals("faktum_1_navn", this.nøkkel(søknadprosess.dato(1)))
            assertEquals("Ønsker frå dato", this.oversett(søknadprosess.dato(1)))
            assertEquals("heltall navn", this.oversett(søknadprosess.heltall(2)))
            søknadprosess.generator(2).besvar(2)
            søknadprosess.dato("3.1").besvar(1.januar)
            assertEquals("En dato oversettelse", this.oversett(søknadprosess.dato("3.1")))
        }
    }

    @Test
    fun `returnerer default språk om verdi definert språk ikke finnes`() {
        with(Språk(lokal = Locale.JAPANESE, versjonId = versjonId)) {
            assertEquals("Ønsker fra dato", this.oversett(søknad.dato(1)))
        }
    }
}
