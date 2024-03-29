package no.nav.dagpenger.model.unit.faktum

import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.dato
import no.nav.dagpenger.model.factory.UtledetFaktumFactory.Companion.maks
import no.nav.dagpenger.model.faktum.Fakta
import no.nav.dagpenger.model.faktum.Faktum
import no.nav.dagpenger.model.helpers.januar
import no.nav.dagpenger.model.helpers.testSøknadprosess
import no.nav.dagpenger.model.helpers.testversjon
import no.nav.dagpenger.model.seksjon.Prosess
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDate
import kotlin.test.assertEquals

internal class UtledetFaktumTest {
    private lateinit var prosess: Prosess
    private lateinit var maks4: Faktum<LocalDate>
    private lateinit var maks3: Faktum<LocalDate>
    private lateinit var dato1: Faktum<LocalDate>
    private lateinit var dato2: Faktum<LocalDate>
    private lateinit var dato5: Faktum<LocalDate>
    private lateinit var dato6: Faktum<LocalDate>

    @BeforeEach
    fun setup() {
        prosess = Fakta(
            testversjon,
            maks dato "maks dato" av 1 og 3 og 6 id 4,
            maks dato "maks dato" av 1 og 2 og 5 id 3,
            dato faktum "dato1" id 1,
            dato faktum "dato2" id 2,
            dato faktum "dato5" id 5,
            dato faktum "dato6" id 6
        ).testSøknadprosess()

        maks4 = prosess dato 4
        maks3 = prosess dato 3
        dato1 = prosess dato 1
        dato2 = prosess dato 2
        dato5 = prosess dato 5
        dato6 = prosess dato 6
    }

    @Test
    fun `støtte for faktum som utledes fra andre faktum`() {
        assertThrows<IllegalStateException> { maks3.svar() }

        dato1.besvar(2.januar)
        dato2.besvar(2.januar)
        assertThrows<IllegalStateException> { maks3.svar() }
        dato5.besvar(1.januar)

        assertEquals(2.januar, maks3.svar())
    }

    @Test
    fun `støtte for faktum som utledes av andre utledede faktum`() {
        dato1.besvar(2.januar)
        dato2.besvar(2.januar)
        dato5.besvar(1.januar)

        assertThrows<IllegalStateException> { maks4.svar() }

        dato6.besvar(3.januar)

        assertEquals(3.januar, maks4.svar())
    }
}
