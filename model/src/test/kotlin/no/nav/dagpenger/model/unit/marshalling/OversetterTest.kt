package no.nav.dagpenger.model.unit.marshalling

import no.nav.dagpenger.model.marshalling.Oversetter.Companion.nynorsk
import no.nav.dagpenger.model.marshalling.oversett
import org.junit.jupiter.api.Test
import java.util.Locale
import kotlin.test.assertEquals

class OversetterTest {

    @Test
    fun `skal oversette fakta fra bokmål til nynorsk `() {
        "Ønsker fra dato".oversett(nynorsk).also {
            assertEquals("Ønsker frå dato", it)
        }
    }

    @Test
    fun `returnerer default språk om verdi definert språk ikke finnes`() {
        "Ønsker fra dato".oversett(Locale.JAPANESE).also {
            assertEquals("Ønsker fra dato", it)
        }
    }
}
