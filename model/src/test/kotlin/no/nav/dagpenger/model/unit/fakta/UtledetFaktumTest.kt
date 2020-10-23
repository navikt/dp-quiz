package no.nav.dagpenger.model.unit.fakta

import no.nav.dagpenger.model.fakta.FaktumNavn
import no.nav.dagpenger.model.fakta.faktum
import no.nav.dagpenger.model.helpers.dimisjonsdato
import no.nav.dagpenger.model.helpers.eksempelSøknad
import no.nav.dagpenger.model.helpers.januar
import no.nav.dagpenger.model.helpers.sisteDagMedLønn
import no.nav.dagpenger.model.helpers.søknadsdato
import no.nav.dagpenger.model.helpers.virkningstidspunkt
import no.nav.dagpenger.model.helpers.ønsketdato
import no.nav.dagpenger.model.regel.MAKS_DATO
import no.nav.dagpenger.model.subsumsjon.Subsumsjon
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

internal class UtledetFaktumTest {
    private lateinit var comp: Subsumsjon

    @BeforeEach
    fun setup() {
        comp = eksempelSøknad().rootSubsumsjon
    }

    @Test
    fun `støtte for faktum som utledes fra andre faktum`() {
        assertThrows<IllegalStateException> { virkningstidspunkt.svar() }

        ønsketdato.besvar(2.januar)
        søknadsdato.besvar(2.januar)
        assertThrows<IllegalStateException> { virkningstidspunkt.svar() }
        sisteDagMedLønn.besvar(1.januar)

        assertEquals(2.januar, virkningstidspunkt.svar())
    }

    @Test
    fun `støtte for faktum som utledes av andre utledede faktum`() {
        ønsketdato.besvar(2.januar)
        søknadsdato.besvar(2.januar)
        sisteDagMedLønn.besvar(1.januar)

        val blurp = setOf(virkningstidspunkt, dimisjonsdato).faktum(FaktumNavn(1, "Blurp dato"), MAKS_DATO)

        assertThrows<IllegalStateException> { blurp.svar() }

        dimisjonsdato.besvar(3.januar)

        assertEquals(3.januar, blurp.svar())
    }
}
