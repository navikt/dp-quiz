package no.nav.dagpenger.model.unit.marshalling

import no.nav.dagpenger.model.marshalling.nynorsk
import no.nav.dagpenger.model.marshalling.oversett
import org.junit.jupiter.api.Test
import java.util.Locale
import kotlin.test.assertEquals

class OversetterTest {

    @Test
    fun `skal oversette fakta fra bokmål til nynorsk `() {
        "v1_faktum_1".oversett(nynorsk).also {
            assertEquals("Ønsker frå dato", it)
        }
    }

    @Test
    fun `returnerer default språk om verdi definert språk ikke finnes`() {
        "v1_faktum_1".oversett(Locale.JAPANESE).also {
            assertEquals("Ønsker fra dato", it)
        }
    }
}
