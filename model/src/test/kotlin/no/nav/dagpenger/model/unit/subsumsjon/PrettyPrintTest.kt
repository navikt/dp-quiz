package no.nav.dagpenger.model.unit.subsumsjon

import no.nav.dagpenger.model.helpers.NyttEksempel
import no.nav.dagpenger.model.seksjon.Faktagrupper
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class PrettyPrintTest {
    private lateinit var faktagrupper: Faktagrupper
    private val antallSubsumsjoner = 17

    @BeforeEach
    fun setUp() {
        faktagrupper = NyttEksempel().faktagrupper
    }

    @Test
    fun `printer ett subsumsjonstre uten verdier`() {
        faktagrupper.rootSubsumsjon.toString().also {
            assertEquals(antallSubsumsjoner, Regex("ukjent").findAll(it).count())
        }
    }

    @Test
    fun `printer ett subsumsjonstre med fakta`() {
    }

    @Test
    fun `printer ett subsumsjonstre med genererte fakta`() {
        faktagrupper.heltall(15).besvar(3)
        faktagrupper.rootSubsumsjon.toString().also {
            assertEquals(antallSubsumsjoner + 1 + (3 * 3), Regex("ukjent").findAll(it).count())
        }
    }
}
