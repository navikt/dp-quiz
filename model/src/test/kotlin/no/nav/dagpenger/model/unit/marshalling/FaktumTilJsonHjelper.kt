package no.nav.dagpenger.model.unit.marshalling

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.dagpenger.model.faktum.Periode
import no.nav.dagpenger.model.marshalling.FaktumTilJsonHjelper.putR
import org.junit.jupiter.api.Test
import java.time.LocalDate
import kotlin.test.assertEquals

internal class FaktumTilJsonHjelper {
    internal class FaktumTilJsonHjelperTest {
        @Test
        fun `Serialiser ikke Periode tom felt som er null `() {
            val dato = LocalDate.of(2022, 8, 26)
            val node =
                jacksonObjectMapper().createObjectNode().also {
                    it.putR(svar = Periode(dato, null))
                }
            assertEquals("""{"svar":{"fom":"2022-08-26"}}""", node.toString())
        }
    }
}
