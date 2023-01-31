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
import no.nav.dagpenger.model.seksjon.Seksjon
import no.nav.dagpenger.model.seksjon.Utredningsprosess
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@Suppress("UNCHECKED_CAST")
internal class FaktaSubsumsjonTest {

    private lateinit var utredningsprosess: Utredningsprosess

    @BeforeEach
    fun setUp() {
        utredningsprosess = NyttEksempel().utredningsprosess
    }

    @Test
    fun `Faktagrupper subsumsjon integrasjonstest`() {
        utredningsprosess.nesteFakta().also { fakta ->
            assertEquals(1, fakta.size)
            assertIder(fakta, 1)
        }

        assertEquals(utredningsprosess[0], utredningsprosess.nesteSeksjoner().first())
        assertEquals(2, utredningsprosess[0].fakta().size)
        assertIder(utredningsprosess[0].fakta(), 1, 2)

        utredningsprosess.boolsk(1).besvar(true)
        utredningsprosess.dato(2).besvar(31.desember)
        utredningsprosess.nesteFakta().also { fakta ->
            assertEquals(3, fakta.size)
            assertIder(fakta, 3, 4, 5)
        }

        assertEquals(utredningsprosess[3], utredningsprosess.nesteSeksjoner().first())
        assertEquals(5, utredningsprosess[3].fakta().size)

        assertIder(utredningsprosess[3].fakta(), 3, 4, 5, 345, 13)
        utredningsprosess.dato(3).besvar(1.januar)
        utredningsprosess.dato(4).besvar(2.januar)
        utredningsprosess.dato(5).besvar(3.januar)
        utredningsprosess.nesteFakta().also { fakta ->
            assertEquals(1, fakta.size)
            assertIder(fakta, 10)
        }

        assertEquals(utredningsprosess[4], utredningsprosess.nesteSeksjoner().first())
        assertEquals(2, utredningsprosess[4].fakta().size)
        assertIder(utredningsprosess[4].fakta(), 10, 11)
        utredningsprosess.boolsk(10).besvar(false)
        utredningsprosess.nesteFakta().also { fakta ->
            assertEquals(1, fakta.size)
            assertIder(fakta, 11)
        }

        assertEquals(utredningsprosess[4], utredningsprosess.nesteSeksjoner().first())
        assertEquals(2, utredningsprosess[4].fakta().size)
        assertIder(utredningsprosess[4].fakta(), 10, 11)
        utredningsprosess.dokument(11).besvar(Dokument(4.januar, "urn:nid:sse"))

        utredningsprosess.nesteFakta().also { fakta ->
            assertEquals(2, fakta.size)
            assertIder(fakta, 6, 8)
        }

        assertEquals(utredningsprosess[1], utredningsprosess.nesteSeksjoner().first())
        assertEquals(4, utredningsprosess[1].fakta().size)
        assertIder(utredningsprosess[1].fakta(), 6, 7, 8, 9)
        utredningsprosess.inntekt(6).besvar(20000.månedlig)
        utredningsprosess.inntekt(7).besvar(10000.månedlig)
        utredningsprosess.inntekt(8).besvar(5000.månedlig)
        utredningsprosess.inntekt(9).besvar(2500.månedlig)

        utredningsprosess.nesteFakta().also { fakta ->
            assertEquals(1, fakta.size)
            assertIder(fakta, 15)
        }

        assertEquals(utredningsprosess[2], utredningsprosess.nesteSeksjoner().first())
        assertEquals(1, utredningsprosess[2].fakta().size)
        assertIder(utredningsprosess[2].fakta(), 15)
        utredningsprosess.generator(15).besvar(2)
        assertEquals(3, utredningsprosess[2].fakta().size) // Genererte 2 til
        utredningsprosess.nesteFakta().also { fakta ->
            assertEquals(1, fakta.size) // Feltene i første genererte subsumsjon
            assertEquals(listOf("16.1"), fakta.map { it.id })
        }
        (utredningsprosess[2].first { it.id == "16.1" } as Faktum<Int>).besvar(17)

        utredningsprosess.nesteFakta().also { fakta ->
            assertEquals(1, fakta.size)
            assertEquals(listOf("17.1"), fakta.map { it.id })
        }
        assertEquals(utredningsprosess[7], utredningsprosess.nesteSeksjoner().first())
        assertEquals(2, utredningsprosess[7].fakta().size)
        assertEquals(listOf("17.1", "18.1"), utredningsprosess[7].fakta().map { it.id })
        (utredningsprosess[7].first { it.id == "17.1" } as Faktum<Boolean>).besvar(true)

        utredningsprosess.nesteFakta().also { fakta ->
            assertEquals(1, fakta.size) // Feltene i første genererte subsumsjon
            assertEquals(listOf("16.2"), fakta.map { it.id })
        }
        assertEquals(utredningsprosess[2], utredningsprosess.nesteSeksjoner().first())
        assertEquals(3, utredningsprosess[2].fakta().size)
        assertEquals(listOf("15", "16.1", "16.2"), utredningsprosess[2].fakta().map { it.id })
        (utredningsprosess[2].first { it.id == "16.2" } as Faktum<Int>).besvar(19)

        utredningsprosess.nesteFakta().also { fakta ->
            assertEquals(1, fakta.size)
            assertIder(fakta, 14)
        }

        assertEquals(utredningsprosess[9], utredningsprosess.nesteSeksjoner().first())
        assertEquals(7, utredningsprosess[9].fakta().size)
        assertEquals(
            listOf("6", "7", "12", "14", "16.1", "16.2", "19").sorted(),
            utredningsprosess[9].fakta().map { it.id }.sorted()
        )
        utredningsprosess.boolsk(14).besvar(true)
        utredningsprosess.nesteFakta().also { fakta ->
            assertEquals(0, fakta.size)
            assertEquals(emptySet<GrunnleggendeFaktum<*>>(), fakta)
        }
    }

    @Test
    fun `Avvisning av søker faktum`() {
        utredningsprosess.boolsk(1).besvar(true)
        utredningsprosess.dato(2).besvar(31.desember)
        utredningsprosess.dato(3).besvar(1.januar)
        utredningsprosess.dato(4).besvar(2.januar)
        utredningsprosess.dato(5).besvar(3.januar)

        utredningsprosess.nesteFakta().also { fakta ->
            assertEquals(1, fakta.size)
            assertEquals(setOf(utredningsprosess.boolsk(10)), fakta)
        }

        utredningsprosess.boolsk(10).besvar(false)
        utredningsprosess.dokument(11).besvar(Dokument(1.januar, "urn:nid:sse"))
        utredningsprosess.boolsk(12).besvar(false)

        assertEquals(null, utredningsprosess.resultat())

        utredningsprosess.dato(13).besvar(1.februar)
        assertEquals(true, utredningsprosess.resultat())

        utredningsprosess.boolsk(19).besvar(false)
        assertEquals(false, utredningsprosess.resultat())
    }

    private fun assertIder(fakta: Set<Faktum<*>>, vararg ider: Int) {
        assertIder(fakta, *(ider.map { it.toString() }.toTypedArray()))
    }

    private fun assertIder(fakta: Set<Faktum<*>>, vararg ider: String) {
        assertEquals(ider.toList(), fakta.map { it.id })
    }

    private fun Seksjon.fakta() = this.filter { it !is TemplateFaktum }.toSet()
}
