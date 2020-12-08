package no.nav.dagpenger.model.unit.marshalling

import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.dato
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.heltall
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.marshalling.Språk
import no.nav.dagpenger.model.marshalling.Språk.Companion.nynorsk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.Locale

internal class SpråkTest {

    private val søknad = Søknad(
        1,
        dato faktum "dato navn" id 1,
        heltall faktum "heltall navn" id 2
    )

    @Test
    fun `skal oversette fakta fra bokmål til nynorsk `() {
        with(Språk(lokal = nynorsk, versjonId = 1)) {
            assertEquals("faktum_1_navn", this.nøkkel(søknad.dato(1)))
            assertEquals("Ønsker frå dato", this.oversett(søknad.dato(1)))
            assertEquals("heltall navn", this.oversett(søknad.heltall(2)))
        }
    }

    @Test
    fun `returnerer default språk om verdi definert språk ikke finnes`() {
        with(Språk(lokal = Locale.JAPANESE, versjonId = 1)) {
            assertEquals("Ønsker fra dato", this.oversett(søknad.dato(1)))
        }
    }
}
