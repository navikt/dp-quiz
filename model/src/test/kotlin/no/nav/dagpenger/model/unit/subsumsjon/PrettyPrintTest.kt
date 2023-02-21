package no.nav.dagpenger.model.unit.subsumsjon

import no.nav.dagpenger.model.helpers.NyttEksempel
import no.nav.dagpenger.model.seksjon.Prosess
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class PrettyPrintTest {
    private lateinit var prosess: Prosess
    private val antallSubsumsjoner = 17

    @BeforeEach
    fun setUp() {
        prosess = NyttEksempel().prosess
    }

    @Test
    fun `printer ett subsumsjonstre uten verdier`() {
        prosess.rootSubsumsjon.toString().also {
            assertEquals(antallSubsumsjoner, Regex("ukjent").findAll(it).count())
        }
    }

    @Test
    fun `printer ett subsumsjonstre med fakta`() {
    }

    @Test
    fun `printer ett subsumsjonstre med genererte fakta`() {
        prosess.heltall(15).besvar(3)
        prosess.rootSubsumsjon.toString().also {
            assertEquals(antallSubsumsjoner + 1 + (3 * 3), Regex("ukjent").findAll(it).count())
        }
    }
}
