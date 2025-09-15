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
import no.nav.dagpenger.model.seksjon.Prosess
import no.nav.dagpenger.model.seksjon.Seksjon
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@Suppress("UNCHECKED_CAST")
internal class FaktaSubsumsjonTest {
    private lateinit var prosess: Prosess

    @BeforeEach
    fun setUp() {
        prosess = NyttEksempel().prosess
    }

    @Test
    fun `Faktagrupper subsumsjon integrasjonstest`() {
        prosess.nesteFakta().also { fakta ->
            assertEquals(1, fakta.size)
            assertIder(fakta, 1)
        }

        assertEquals(prosess[0], prosess.nesteSeksjoner().first())
        assertEquals(2, prosess[0].fakta().size)
        assertIder(prosess[0].fakta(), 1, 2)

        prosess.boolsk(1).besvar(true)
        prosess.dato(2).besvar(31.desember)
        prosess.nesteFakta().also { fakta ->
            assertEquals(3, fakta.size)
            assertIder(fakta, 3, 4, 5)
        }

        assertEquals(prosess[3], prosess.nesteSeksjoner().first())
        assertEquals(5, prosess[3].fakta().size)

        assertIder(prosess[3].fakta(), 3, 4, 5, 345, 13)
        prosess.dato(3).besvar(1.januar)
        prosess.dato(4).besvar(2.januar)
        prosess.dato(5).besvar(3.januar)
        prosess.nesteFakta().also { fakta ->
            assertEquals(1, fakta.size)
            assertIder(fakta, 10)
        }

        assertEquals(prosess[4], prosess.nesteSeksjoner().first())
        assertEquals(2, prosess[4].fakta().size)
        assertIder(prosess[4].fakta(), 10, 11)
        prosess.boolsk(10).besvar(false)
        prosess.nesteFakta().also { fakta ->
            assertEquals(1, fakta.size)
            assertIder(fakta, 11)
        }

        assertEquals(prosess[4], prosess.nesteSeksjoner().first())
        assertEquals(2, prosess[4].fakta().size)
        assertIder(prosess[4].fakta(), 10, 11)
        prosess.dokument(11).besvar(Dokument(4.januar, "urn:nid:sse"))

        prosess.nesteFakta().also { fakta ->
            assertEquals(2, fakta.size)
            assertIder(fakta, 6, 8)
        }

        assertEquals(prosess[1], prosess.nesteSeksjoner().first())
        assertEquals(4, prosess[1].fakta().size)
        assertIder(prosess[1].fakta(), 6, 7, 8, 9)
        prosess.inntekt(6).besvar(20000.månedlig)
        prosess.inntekt(7).besvar(10000.månedlig)
        prosess.inntekt(8).besvar(5000.månedlig)
        prosess.inntekt(9).besvar(2500.månedlig)

        prosess.nesteFakta().also { fakta ->
            assertEquals(1, fakta.size)
            assertIder(fakta, 15)
        }

        assertEquals(prosess[2], prosess.nesteSeksjoner().first())
        assertEquals(1, prosess[2].fakta().size)
        assertIder(prosess[2].fakta(), 15)
        prosess.generator(15).besvar(2)
        assertEquals(3, prosess[2].fakta().size) // Genererte 2 til
        prosess.nesteFakta().also { fakta ->
            assertEquals(1, fakta.size) // Feltene i første genererte subsumsjon
            assertEquals(listOf("16.1"), fakta.map { it.id })
        }
        (prosess[2].first { it.id == "16.1" } as Faktum<Int>).besvar(17)

        prosess.nesteFakta().also { fakta ->
            assertEquals(1, fakta.size)
            assertEquals(listOf("17.1"), fakta.map { it.id })
        }
        assertEquals(prosess[7], prosess.nesteSeksjoner().first())
        assertEquals(2, prosess[7].fakta().size)
        assertEquals(listOf("17.1", "18.1"), prosess[7].fakta().map { it.id })
        (prosess[7].first { it.id == "17.1" } as Faktum<Boolean>).besvar(true)

        prosess.nesteFakta().also { fakta ->
            assertEquals(1, fakta.size) // Feltene i første genererte subsumsjon
            assertEquals(listOf("16.2"), fakta.map { it.id })
        }
        assertEquals(prosess[2], prosess.nesteSeksjoner().first())
        assertEquals(3, prosess[2].fakta().size)
        assertEquals(listOf("15", "16.1", "16.2"), prosess[2].fakta().map { it.id })
        (prosess[2].first { it.id == "16.2" } as Faktum<Int>).besvar(19)

        prosess.nesteFakta().also { fakta ->
            assertEquals(1, fakta.size)
            assertIder(fakta, 14)
        }

        assertEquals(prosess[9], prosess.nesteSeksjoner().first())
        assertEquals(7, prosess[9].fakta().size)
        assertEquals(
            listOf("6", "7", "12", "14", "16.1", "16.2", "19").sorted(),
            prosess[9].fakta().map { it.id }.sorted(),
        )
        prosess.boolsk(14).besvar(true)
        prosess.nesteFakta().also { fakta ->
            assertEquals(0, fakta.size)
            assertEquals(emptySet<GrunnleggendeFaktum<*>>(), fakta)
        }
    }

    @Test
    fun `Avvisning av søker faktum`() {
        prosess.boolsk(1).besvar(true)
        prosess.dato(2).besvar(31.desember)
        prosess.dato(3).besvar(1.januar)
        prosess.dato(4).besvar(2.januar)
        prosess.dato(5).besvar(3.januar)

        prosess.nesteFakta().also { fakta ->
            assertEquals(1, fakta.size)
            assertEquals(setOf(prosess.boolsk(10)), fakta)
        }

        prosess.boolsk(10).besvar(false)
        prosess.dokument(11).besvar(Dokument(1.januar, "urn:nid:sse"))
        prosess.boolsk(12).besvar(false)

        assertEquals(null, prosess.resultat())

        prosess.dato(13).besvar(1.februar)
        assertEquals(true, prosess.resultat())

        prosess.boolsk(19).besvar(false)
        assertEquals(false, prosess.resultat())
    }

    private fun assertIder(
        fakta: Set<Faktum<*>>,
        vararg ider: Int,
    ) {
        assertIder(fakta, *(ider.map { it.toString() }.toTypedArray()))
    }

    private fun assertIder(
        fakta: Set<Faktum<*>>,
        vararg ider: String,
    ) {
        assertEquals(ider.toList(), fakta.map { it.id })
    }

    private fun Seksjon.fakta() = this.filter { it !is TemplateFaktum }.toSet()
}
