package no.nav.dagpenger.qamodel.unit

import no.nav.dagpenger.qamodel.helpers.bursdag67
import no.nav.dagpenger.qamodel.helpers.dimisjonsdato
import no.nav.dagpenger.qamodel.helpers.februar
import no.nav.dagpenger.qamodel.helpers.inntektSisteÅr
import no.nav.dagpenger.qamodel.helpers.januar
import no.nav.dagpenger.qamodel.helpers.sisteDagMedLønn
import no.nav.dagpenger.qamodel.helpers.subsumsjonRoot
import no.nav.dagpenger.qamodel.helpers.søknadsdato
import no.nav.dagpenger.qamodel.helpers.ønsketdato
import no.nav.dagpenger.qamodel.port.Inntekt.Companion.månedlig
import no.nav.dagpenger.qamodel.subsumsjon.Subsumsjon
import no.nav.dagpenger.qamodel.subsumsjon.før
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class SammensattSubsumsjonsTest {
    private lateinit var comp: Subsumsjon

    @BeforeEach
    fun setup() {
        comp = subsumsjonRoot()
    }

    @Test
    fun `neste fakta`() {
        assertEquals(10, comp.fakta().size)
        ønsketdato.besvar(2.januar)
        søknadsdato.besvar(2.januar)
        sisteDagMedLønn.besvar(1.januar)
        assertEquals(1, comp.nesteFakta().size)
        bursdag67.besvar(31.januar)
        assertEquals(6, comp.nesteFakta().size)
        inntektSisteÅr.besvar(100000.månedlig)
        dimisjonsdato.besvar(1.januar)
        assertEquals(4, comp.nesteFakta().size)
        assertEquals(10, comp.fakta().size)
        println(comp)
    }

    @Test
    fun `finne en fakta`() {
        assertEquals(2, comp.subsumsjoner(søknadsdato).size)
        assertEquals(listOf(comp[0][0], comp[1][1]), comp.subsumsjoner(søknadsdato))

        assertEquals(4, comp.subsumsjoner(ønsketdato).size)
        assertEquals(
            listOf(
                comp[0][1],
                comp[1][0],
                comp.gyldig.ugyldig[0],
                comp.ugyldig[0]
            ),
            comp.subsumsjoner(ønsketdato)
        )
    }

    @Test
    fun `finne flere fakta`() {
        assertEquals(6, comp.subsumsjoner(ønsketdato, søknadsdato).size)
        assertEquals(
            listOf(
                comp[0][0],
                comp[0][1],
                comp[1][0],
                comp[1][1],
                comp.gyldig.ugyldig[0],
                comp.ugyldig[0]
            ),
            comp.subsumsjoner(ønsketdato, søknadsdato)
        )

        assertEquals(6, comp.subsumsjoner(ønsketdato, bursdag67).size)
        assertEquals(
            listOf(
                comp[0][0],
                comp[0][1],
                comp[0][2],
                comp[1][0],
                comp.gyldig.ugyldig[0],
                comp.ugyldig[0]
            ),
            comp.subsumsjoner(ønsketdato, bursdag67)
        )
    }

    @Test
    fun `Sti`() {
        assertEquals(listOf(comp[1][1]), comp[1][1].sti(comp[1][1]))
        assertEquals(listOf(comp[1], comp[1][1]), comp[1].sti(comp[1][1]))
        assertEquals(listOf(comp), comp.sti(comp))
        assertEquals(listOf(comp, comp[1], comp[1][1]), comp.sti(comp[1][1]))
        assertEquals(listOf(comp, comp.gyldig, comp.gyldig[1]), comp.sti(comp.gyldig[1]))
        assertEquals(listOf(comp, comp.ugyldig, comp.ugyldig[0]), comp.sti(comp.ugyldig[0]))

        assertEquals(emptyList<Subsumsjon>(), comp.sti(dimisjonsdato før bursdag67))
        assertThrows<IndexOutOfBoundsException> {
            comp.sti(comp.ugyldig[0].gyldig)
        }
    }

    @Test
    fun `enkel subsumsjon resultater`() {
        assertEquals(null, comp[0][0].resultat())

        søknadsdato.besvar(1.januar)
        bursdag67.besvar(31.januar)
        assertEquals(true, comp[0][0].resultat())

        søknadsdato.besvar(1.februar)
        assertEquals(false, comp[0][0].resultat())
    }
}
