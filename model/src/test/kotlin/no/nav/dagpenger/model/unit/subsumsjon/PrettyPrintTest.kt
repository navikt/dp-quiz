package no.nav.dagpenger.model.unit.subsumsjon

import no.nav.dagpenger.model.faktagrupper.Faktagrupper
import no.nav.dagpenger.model.helpers.NyttEksempel
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class PrettyPrintTest {
    private lateinit var faktagrupper: Faktagrupper

    @BeforeEach
    fun setUp() {
        faktagrupper = NyttEksempel().faktagrupper
    }

    @Test
    fun `printer ett subsumsjonstre uten verdier`() {
        faktagrupper.rootSubsumsjon.toString().also {
            assertEquals(17, Regex("ukjent").findAll(it).count())
        }
    }

    @Test
    fun `printer ett subsumsjonstre med fakta`() {
    }

    @Test
    fun `printer ett subsumsjonstre med genererte fakta`() {
        faktagrupper.heltall(15).besvar(3)
        faktagrupper.rootSubsumsjon.toString().also {
            assertEquals(17 + 1 + (3 * 3), Regex("ukjent").findAll(it).count())
        }
    }
}
