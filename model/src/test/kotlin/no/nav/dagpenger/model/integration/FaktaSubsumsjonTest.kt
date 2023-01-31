package no.nav.dagpenger.model.integration

import no.nav.dagpenger.model.faktum.Dokument
import no.nav.dagpenger.model.faktum.Faktum
import no.nav.dagpenger.model.faktum.GrunnleggendeFaktum
import no.nav.dagpenger.model.faktum.Inntekt.Companion.månedlig
import no.nav.dagpenger.model.faktum.TemplateFaktum
import no.nav.dagpenger.model.helpers.NyttEksempel
import no.nav.dagpenger.model.helpers.desember
import no.nav.dagpenger.model.helpers.februar
import no.nav.dagpenger.model.helpers.januar
import no.nav.dagpenger.model.seksjon.Faktagrupper
import no.nav.dagpenger.model.seksjon.Seksjon
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@Suppress("UNCHECKED_CAST")
internal class FaktaSubsumsjonTest {

    private lateinit var faktagrupper: Faktagrupper

    @BeforeEach
    fun setUp() {
        faktagrupper = NyttEksempel().faktagrupper
    }

    @Test
    fun `Faktagrupper subsumsjon integrasjonstest`() {
        faktagrupper.nesteFakta().also { fakta ->
            assertEquals(1, fakta.size)
            assertIder(fakta, 1)
        }

        assertEquals(faktagrupper[0], faktagrupper.nesteSeksjoner().first())
        assertEquals(2, faktagrupper[0].fakta().size)
        assertIder(faktagrupper[0].fakta(), 1, 2)

        faktagrupper.boolsk(1).besvar(true)
        faktagrupper.dato(2).besvar(31.desember)
        faktagrupper.nesteFakta().also { fakta ->
            assertEquals(3, fakta.size)
            assertIder(fakta, 3, 4, 5)
        }

        assertEquals(faktagrupper[3], faktagrupper.nesteSeksjoner().first())
        assertEquals(5, faktagrupper[3].fakta().size)

        assertIder(faktagrupper[3].fakta(), 3, 4, 5, 345, 13)
        faktagrupper.dato(3).besvar(1.januar)
        faktagrupper.dato(4).besvar(2.januar)
        faktagrupper.dato(5).besvar(3.januar)
        faktagrupper.nesteFakta().also { fakta ->
            assertEquals(1, fakta.size)
            assertIder(fakta, 10)
        }

        assertEquals(faktagrupper[4], faktagrupper.nesteSeksjoner().first())
        assertEquals(2, faktagrupper[4].fakta().size)
        assertIder(faktagrupper[4].fakta(), 10, 11)
        faktagrupper.boolsk(10).besvar(false)
        faktagrupper.nesteFakta().also { fakta ->
            assertEquals(1, fakta.size)
            assertIder(fakta, 11)
        }

        assertEquals(faktagrupper[4], faktagrupper.nesteSeksjoner().first())
        assertEquals(2, faktagrupper[4].fakta().size)
        assertIder(faktagrupper[4].fakta(), 10, 11)
        faktagrupper.dokument(11).besvar(Dokument(4.januar, "urn:nid:sse"))

        faktagrupper.nesteFakta().also { fakta ->
            assertEquals(2, fakta.size)
            assertIder(fakta, 6, 8)
        }

        assertEquals(faktagrupper[1], faktagrupper.nesteSeksjoner().first())
        assertEquals(4, faktagrupper[1].fakta().size)
        assertIder(faktagrupper[1].fakta(), 6, 7, 8, 9)
        faktagrupper.inntekt(6).besvar(20000.månedlig)
        faktagrupper.inntekt(7).besvar(10000.månedlig)
        faktagrupper.inntekt(8).besvar(5000.månedlig)
        faktagrupper.inntekt(9).besvar(2500.månedlig)

        faktagrupper.nesteFakta().also { fakta ->
            assertEquals(1, fakta.size)
            assertIder(fakta, 15)
        }

        assertEquals(faktagrupper[2], faktagrupper.nesteSeksjoner().first())
        assertEquals(1, faktagrupper[2].fakta().size)
        assertIder(faktagrupper[2].fakta(), 15)
        faktagrupper.generator(15).besvar(2)
        assertEquals(3, faktagrupper[2].fakta().size) // Genererte 2 til
        faktagrupper.nesteFakta().also { fakta ->
            assertEquals(1, fakta.size) // Feltene i første genererte subsumsjon
            assertEquals(listOf("16.1"), fakta.map { it.id })
        }
        (faktagrupper[2].first { it.id == "16.1" } as Faktum<Int>).besvar(17)

        faktagrupper.nesteFakta().also { fakta ->
            assertEquals(1, fakta.size)
            assertEquals(listOf("17.1"), fakta.map { it.id })
        }
        assertEquals(faktagrupper[7], faktagrupper.nesteSeksjoner().first())
        assertEquals(2, faktagrupper[7].fakta().size)
        assertEquals(listOf("17.1", "18.1"), faktagrupper[7].fakta().map { it.id })
        (faktagrupper[7].first { it.id == "17.1" } as Faktum<Boolean>).besvar(true)

        faktagrupper.nesteFakta().also { fakta ->
            assertEquals(1, fakta.size) // Feltene i første genererte subsumsjon
            assertEquals(listOf("16.2"), fakta.map { it.id })
        }
        assertEquals(faktagrupper[2], faktagrupper.nesteSeksjoner().first())
        assertEquals(3, faktagrupper[2].fakta().size)
        assertEquals(listOf("15", "16.1", "16.2"), faktagrupper[2].fakta().map { it.id })
        (faktagrupper[2].first { it.id == "16.2" } as Faktum<Int>).besvar(19)

        faktagrupper.nesteFakta().also { fakta ->
            assertEquals(1, fakta.size)
            assertIder(fakta, 14)
        }

        assertEquals(faktagrupper[9], faktagrupper.nesteSeksjoner().first())
        assertEquals(7, faktagrupper[9].fakta().size)
        assertEquals(
            listOf("6", "7", "12", "14", "16.1", "16.2", "19").sorted(),
            faktagrupper[9].fakta().map { it.id }.sorted()
        )
        faktagrupper.boolsk(14).besvar(true)
        faktagrupper.nesteFakta().also { fakta ->
            assertEquals(0, fakta.size)
            assertEquals(emptySet<GrunnleggendeFaktum<*>>(), fakta)
        }
    }

    @Test
    fun `Avvisning av søker faktum`() {
        faktagrupper.boolsk(1).besvar(true)
        faktagrupper.dato(2).besvar(31.desember)
        faktagrupper.dato(3).besvar(1.januar)
        faktagrupper.dato(4).besvar(2.januar)
        faktagrupper.dato(5).besvar(3.januar)

        faktagrupper.nesteFakta().also { fakta ->
            assertEquals(1, fakta.size)
            assertEquals(setOf(faktagrupper.boolsk(10)), fakta)
        }

        faktagrupper.boolsk(10).besvar(false)
        faktagrupper.dokument(11).besvar(Dokument(1.januar, "urn:nid:sse"))
        faktagrupper.boolsk(12).besvar(false)

        assertEquals(null, faktagrupper.resultat())

        faktagrupper.dato(13).besvar(1.februar)
        assertEquals(true, faktagrupper.resultat())

        faktagrupper.boolsk(19).besvar(false)
        assertEquals(false, faktagrupper.resultat())
    }

    private fun assertIder(fakta: Set<Faktum<*>>, vararg ider: Int) {
        assertIder(fakta, *(ider.map { it.toString() }.toTypedArray()))
    }

    private fun assertIder(fakta: Set<Faktum<*>>, vararg ider: String) {
        assertEquals(ider.toList(), fakta.map { it.id })
    }

    private fun Seksjon.fakta() = this.filter { it !is TemplateFaktum }.toSet()
}
