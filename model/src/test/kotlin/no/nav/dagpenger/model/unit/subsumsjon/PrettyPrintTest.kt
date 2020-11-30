package no.nav.dagpenger.model.unit.subsumsjon

import no.nav.dagpenger.model.helpers.NyttEksempel
import no.nav.dagpenger.model.seksjon.Søknadprosess
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class PrettyPrintTest {
    private lateinit var søknadprosess: Søknadprosess

    @BeforeEach
    fun setUp() {
        søknadprosess = NyttEksempel().søknadprosess
    }

    @Test
    fun `printer ett subsumsjonstre uten verdier`() {
        søknadprosess.rootSubsumsjon.toString().also {
            assertEquals(19, Regex("ukjent").findAll(it).count())
        }
    }

    @Test
    fun `printer ett subsumsjonstre med fakta`() {
    }

    @Test
    fun `printer ett subsumsjonstre med genererte fakta`() {
        søknadprosess.heltall(15).besvar(3)
        søknadprosess.rootSubsumsjon.toString().also {
            assertEquals(19 + 1 + (3 * 3), Regex("ukjent").findAll(it).count())
        }
    }
}
