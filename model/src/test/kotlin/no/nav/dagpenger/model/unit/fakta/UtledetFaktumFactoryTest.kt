package no.nav.dagpenger.model.unit.fakta

import no.nav.dagpenger.model.fakta.FaktumFactory.Companion.dato
import no.nav.dagpenger.model.fakta.UtledetFaktumFactory.Companion.maks
import org.junit.jupiter.api.Test

internal class UtledetFaktumFactoryTest {

    @Test
    fun `maks dato`() {
        val dato1 = dato faktum "dato1" id 1
        val dato2 = dato faktum "dato1" id 2
        val dato3 = dato faktum "dato1" id 3
        val faktum = maks dato "maks dato" av dato1 og dato2 og dato3 id 123
    }
}
