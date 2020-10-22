package no.nav.dagpenger.model.integration

import no.nav.dagpenger.model.fakta.Dokument
import no.nav.dagpenger.model.fakta.Faktum
import no.nav.dagpenger.model.fakta.GrunnleggendeFaktum
import no.nav.dagpenger.model.fakta.Inntekt.Companion.månedlig
import no.nav.dagpenger.model.fakta.Rolle
import no.nav.dagpenger.model.fakta.TemplateFaktum
import no.nav.dagpenger.model.helpers.NyEksempel
import no.nav.dagpenger.model.helpers.desember
import no.nav.dagpenger.model.helpers.februar
import no.nav.dagpenger.model.helpers.januar
import no.nav.dagpenger.model.søknad.Seksjon
import no.nav.dagpenger.model.søknad.Søknad
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class SøknadSubsumsjonTest {

    private lateinit var søknad: Søknad

    @BeforeEach
    fun setUp() {
        søknad = NyEksempel().søknad
    }

    @Test
    fun `Søknad subsumsjon integrasjonstest`() {
        søknad.nesteFakta().also { fakta ->
            assertEquals(5, fakta.size)
            assertIder(fakta, 1, 2, 3, 4, 5)
        }

        assertEquals(søknad[0], søknad.nesteSeksjon())
        assertEquals(2, søknad[0].fakta().size)
        assertIder(søknad[0].fakta(), 1, 2)

        søknad.ja(1).besvar(true, Rolle.nav)
        søknad.dato(2).besvar(31.desember, Rolle.nav)
        søknad.nesteFakta().also { fakta ->
            assertEquals(3, fakta.size)
            assertIder(fakta, 3, 4, 5)
        }

        assertEquals(søknad[3], søknad.nesteSeksjon())
        assertEquals(5, søknad[3].fakta().size)

        assertIder(søknad[3].fakta(), 3, 4, 5, 345, 13)
        søknad.dato(3).besvar(1.januar)
        søknad.dato(4).besvar(2.januar)
        søknad.dato(5).besvar(3.januar)
        søknad.nesteFakta().also { fakta ->
            assertEquals(1, fakta.size)
            assertIder(fakta, 10)
        }

        assertEquals(søknad[4], søknad.nesteSeksjon())
        assertEquals(2, søknad[4].fakta().size)
        assertIder(søknad[4].fakta(), 10, 11)
        søknad.ja(10).besvar(false)
        søknad.nesteFakta().also { fakta ->
            assertEquals(1, fakta.size)
            assertIder(fakta, 11)
        }

        assertEquals(søknad[4], søknad.nesteSeksjon())
        assertEquals(2, søknad[4].fakta().size)
        assertIder(søknad[4].fakta(), 10, 11)
        søknad.dokument(11).besvar(Dokument(4.januar))

        søknad.nesteFakta().also { fakta ->
            assertEquals(4, fakta.size)
            assertIder(fakta, 6, 8, 7, 9)
        }

        assertEquals(søknad[1], søknad.nesteSeksjon())
        assertEquals(4, søknad[1].fakta().size)
        assertIder(søknad[1].fakta(), 6, 7, 8, 9)

        søknad.inntekt(6).besvar(20000.månedlig, Rolle.nav)
        søknad.inntekt(7).besvar(10000.månedlig, Rolle.nav)
        søknad.inntekt(8).besvar(5000.månedlig, Rolle.nav)
        søknad.inntekt(9).besvar(2500.månedlig, Rolle.nav)
        søknad.nesteFakta().also { fakta ->
            assertEquals(1, fakta.size)
            assertIder(fakta, 15)
        }

        assertEquals(søknad[2], søknad.nesteSeksjon())
        assertEquals(1, søknad[2].fakta().size)
        assertIder(søknad[2].fakta(), 15)
        søknad.generator(15).besvar(2, Rolle.nav)
        assertEquals(3, søknad[2].fakta().size) // Genererte 2 til
        søknad.nesteFakta().also { fakta ->
            assertEquals(2, fakta.size) // Feltene i første genererte subsumsjon
            assertEquals(listOf("16.1", "16.2"), fakta.map { it.id })
        }

        assertEquals(søknad[2], søknad.nesteSeksjon())
        assertEquals(3, søknad[2].fakta().size)
        assertEquals(listOf("15", "16.1", "16.2"), søknad[2].fakta().map { it.id })
        (søknad[2].first { it.id == "16.1" } as Faktum<Int>).besvar(17, Rolle.nav)
        (søknad[2].first { it.id == "16.2" } as Faktum<Int>).besvar(19, Rolle.nav)
        søknad.nesteFakta().also { fakta ->
            assertEquals(1, fakta.size)
            assertEquals(listOf("17.1"), fakta.map { it.id })
        }

        assertEquals(søknad[7], søknad.nesteSeksjon())
        assertEquals(2, søknad[7].fakta().size)
        assertEquals(listOf("16.1", "17.1"), søknad[7].fakta().map { it.id })
        (søknad[7].first { it.id == "17.1" } as Faktum<Boolean>).besvar(true)
        søknad.nesteFakta().also { fakta ->
            assertEquals(1, fakta.size)
            assertIder(fakta, 14)
        }

        assertEquals(søknad[9], søknad.nesteSeksjon())
        assertEquals(7, søknad[9].fakta().size)
        assertEquals(listOf("6", "7", "12", "14", "18.1", "18.2", "19").sorted(), søknad[9].fakta().map { it.id }.sorted())
        søknad.ja(14).besvar(true, Rolle.saksbehandler)
        søknad.nesteFakta().also { fakta ->
            assertEquals(0, fakta.size)
            assertEquals(emptySet<GrunnleggendeFaktum<*>>(), fakta)
        }
    }

    @Test
    fun `Avvisning av søker faktum`() {
        søknad.ja(1).besvar(true, Rolle.nav)
        søknad.dato(2).besvar(31.desember, Rolle.nav)
        søknad.dato(3).besvar(1.januar)
        søknad.dato(4).besvar(2.januar)
        søknad.dato(5).besvar(3.januar)

        søknad.nesteFakta().also { fakta ->
            assertEquals(1, fakta.size)
            assertEquals(setOf(søknad.ja(10)), fakta)
        }

        søknad.ja(10).besvar(false)
        søknad.dokument(11).besvar(Dokument(1.januar))
        søknad.ja(12).besvar(false, Rolle.saksbehandler)

        søknad.nesteFakta().also { fakta ->
            assertEquals(1, fakta.size)
            assertEquals(setOf(søknad.dato(13)), fakta)
        }

        søknad.dato(13).besvar(1.februar)
        assertEquals(true, søknad.resultat())

        søknad.ja(19).besvar(false, Rolle.saksbehandler)
        assertEquals(false, søknad.resultat())
    }

    private fun assertIder(fakta: Set<Faktum<*>>, vararg ider: Int) {
        assertIder(fakta, *(ider.map { it.toString() }.toTypedArray()))
    }

    private fun assertIder(fakta: Set<Faktum<*>>, vararg ider: String) {
        assertEquals(ider.toList(), fakta.map { it.id })
    }

    private fun Seksjon.fakta() = this.filter { it !is TemplateFaktum }.toSet()
}
