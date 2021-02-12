package no.nav.dagpenger.model.unit.faktum

import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.desimaltall
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.inntekt
import no.nav.dagpenger.model.factory.UtledetFaktumFactory.Companion.multiplikasjon
import no.nav.dagpenger.model.faktum.Faktum
import no.nav.dagpenger.model.faktum.Inntekt
import no.nav.dagpenger.model.faktum.Inntekt.Companion.årlig
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.helpers.testSøknadprosess
import no.nav.dagpenger.model.seksjon.Søknadprosess
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

internal class MultiplikasjonFaktumTest {
    private lateinit var søknadprosess: Søknadprosess
    private lateinit var faktor: Faktum<Double>
    private lateinit var g: Faktum<Inntekt>
    private lateinit var multiplisert: Faktum<Inntekt>

    @BeforeEach
    fun setup() {
        søknadprosess = Søknad(
            0,
            desimaltall faktum "faktor" id 1,
            inntekt faktum "g" id 2,
            multiplikasjon inntekt "multiplikasjon" av 1 ganger 2 id 3
        ).testSøknadprosess()

        faktor = søknadprosess desimaltall 1
        g = søknadprosess inntekt 2
        multiplisert = søknadprosess inntekt 3
    }

    @Test
    fun `multipliserer`() {
        assertThrows<IllegalStateException> { multiplisert.svar() }

        faktor.besvar(1.5)
        g.besvar(100.årlig)

        assertEquals(150.årlig, multiplisert.svar())
    }
}
