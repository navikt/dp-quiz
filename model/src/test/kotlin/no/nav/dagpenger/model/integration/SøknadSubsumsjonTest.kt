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
import no.nav.dagpenger.model.seksjon.Søknadprosess
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class SøknadSubsumsjonTest {

    private lateinit var søknadprosess: Søknadprosess

    @BeforeEach
    fun setUp() {
        søknadprosess = NyttEksempel().søknadprosess
    }

    @Test
    fun `Faktagrupper subsumsjon integrasjonstest`() {
        søknadprosess.nesteFakta().also { fakta ->
            assertEquals(5, fakta.size)
            assertIder(fakta, 1, 2, 3, 4, 5)
        }

        assertEquals(søknadprosess[0], søknadprosess.nesteSeksjoner().first())
        assertEquals(2, søknadprosess[0].fakta().size)
        assertIder(søknadprosess[0].fakta(), 1, 2)

        søknadprosess.ja(1).besvar(true)
        søknadprosess.dato(2).besvar(31.desember)
        søknadprosess.nesteFakta().also { fakta ->
            assertEquals(3, fakta.size)
            assertIder(fakta, 3, 4, 5)
        }

        assertEquals(søknadprosess[3], søknadprosess.nesteSeksjoner().first())
        assertEquals(5, søknadprosess[3].fakta().size)

        assertIder(søknadprosess[3].fakta(), 3, 4, 5, 345, 13)
        søknadprosess.dato(3).besvar(1.januar)
        søknadprosess.dato(4).besvar(2.januar)
        søknadprosess.dato(5).besvar(3.januar)
        søknadprosess.nesteFakta().also { fakta ->
            assertEquals(1, fakta.size)
            assertIder(fakta, 10)
        }

        assertEquals(søknadprosess[4], søknadprosess.nesteSeksjoner().first())
        assertEquals(2, søknadprosess[4].fakta().size)
        assertIder(søknadprosess[4].fakta(), 10, 11)
        søknadprosess.ja(10).besvar(false)
        søknadprosess.nesteFakta().also { fakta ->
            assertEquals(1, fakta.size)
            assertIder(fakta, 11)
        }

        assertEquals(søknadprosess[4], søknadprosess.nesteSeksjoner().first())
        assertEquals(2, søknadprosess[4].fakta().size)
        assertIder(søknadprosess[4].fakta(), 10, 11)
        søknadprosess.dokument(11).besvar(Dokument(4.januar))

        søknadprosess.nesteFakta().also { fakta ->
            assertEquals(4, fakta.size)
            assertIder(fakta, 6, 8, 7, 9)
        }

        assertEquals(søknadprosess[1], søknadprosess.nesteSeksjoner().first())
        assertEquals(4, søknadprosess[1].fakta().size)
        assertIder(søknadprosess[1].fakta(), 6, 7, 8, 9)

        søknadprosess.inntekt(6).besvar(20000.månedlig)
        søknadprosess.inntekt(7).besvar(10000.månedlig)
        søknadprosess.inntekt(8).besvar(5000.månedlig)
        søknadprosess.inntekt(9).besvar(2500.månedlig)
        søknadprosess.nesteFakta().also { fakta ->
            assertEquals(1, fakta.size)
            assertIder(fakta, 15)
        }

        assertEquals(søknadprosess[2], søknadprosess.nesteSeksjoner().first())
        assertEquals(1, søknadprosess[2].fakta().size)
        assertIder(søknadprosess[2].fakta(), 15)
        søknadprosess.generator(15).besvar(2)
        assertEquals(3, søknadprosess[2].fakta().size) // Genererte 2 til
        søknadprosess.nesteFakta().also { fakta ->
            assertEquals(2, fakta.size) // Feltene i første genererte subsumsjon
            assertEquals(listOf("16.1", "16.2"), fakta.map { it.id })
        }

        assertEquals(søknadprosess[2], søknadprosess.nesteSeksjoner().first())
        assertEquals(3, søknadprosess[2].fakta().size)
        assertEquals(listOf("15", "16.1", "16.2"), søknadprosess[2].fakta().map { it.id })
        (søknadprosess[2].first { it.id == "16.1" } as Faktum<Int>).besvar(17)
        (søknadprosess[2].first { it.id == "16.2" } as Faktum<Int>).besvar(19)
        søknadprosess.nesteFakta().also { fakta ->
            assertEquals(1, fakta.size)
            assertEquals(listOf("17.1"), fakta.map { it.id })
        }

        assertEquals(søknadprosess[7], søknadprosess.nesteSeksjoner().first())
        assertEquals(2, søknadprosess[7].fakta().size)
        assertEquals(listOf("17.1", "18.1"), søknadprosess[7].fakta().map { it.id })
        (søknadprosess[7].first { it.id == "17.1" } as Faktum<Boolean>).besvar(true)
        søknadprosess.nesteFakta().also { fakta ->
            assertEquals(1, fakta.size)
            assertIder(fakta, 14)
        }

        assertEquals(søknadprosess[9], søknadprosess.nesteSeksjoner().first())
        assertEquals(7, søknadprosess[9].fakta().size)
        assertEquals(listOf("6", "7", "12", "14", "16.1", "16.2", "19").sorted(), søknadprosess[9].fakta().map { it.id }.sorted())
        søknadprosess.ja(14).besvar(true)
        søknadprosess.nesteFakta().also { fakta ->
            assertEquals(0, fakta.size)
            assertEquals(emptySet<GrunnleggendeFaktum<*>>(), fakta)
        }
    }

    @Test
    fun `Avvisning av søker faktum`() {
        søknadprosess.ja(1).besvar(true)
        søknadprosess.dato(2).besvar(31.desember)
        søknadprosess.dato(3).besvar(1.januar)
        søknadprosess.dato(4).besvar(2.januar)
        søknadprosess.dato(5).besvar(3.januar)

        søknadprosess.nesteFakta().also { fakta ->
            assertEquals(1, fakta.size)
            assertEquals(setOf(søknadprosess.ja(10)), fakta)
        }

        søknadprosess.ja(10).besvar(false)
        søknadprosess.dokument(11).besvar(Dokument(1.januar))
        søknadprosess.ja(12).besvar(false)

        assertEquals(null, søknadprosess.resultat())

        søknadprosess.dato(13).besvar(1.februar)
        assertEquals(null, søknadprosess.resultat())

        søknadprosess.ja(19).besvar(false)
        assertEquals(null, søknadprosess.resultat())
    }

    private fun assertIder(fakta: Set<Faktum<*>>, vararg ider: Int) {
        assertIder(fakta, *(ider.map { it.toString() }.toTypedArray()))
    }

    private fun assertIder(fakta: Set<Faktum<*>>, vararg ider: String) {
        assertEquals(ider.toList(), fakta.map { it.id })
    }

    private fun Seksjon.fakta() = this.filter { it !is TemplateFaktum }.toSet()
}
